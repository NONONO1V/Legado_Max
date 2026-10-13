package io.legado.app.help.config

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.os.Build
import android.util.LruCache
import androidx.core.net.toUri
import io.legado.app.exception.NoStackTraceException
import io.legado.app.utils.isContentScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/**
 * 界面字体仓库：把用户挑选的 ttf/otf 装成 App 私有副本后再加载。
 *
 * 之所以要有"私有副本"：用户可能从 SAF 目录（content://）选字体，
 * 这类 URI 的读权限不保证长期有效，重启后直接读原 URI 会失败。
 * 这里在安装时把字节拷进 `filesDir/uiFont/`，之后永远读自己的副本。
 *
 * 加载分两档：
 * - API 29+：`Typeface.CustomFallbackBuilder(...).setSystemFallback("sans-serif")`，
 *   缺字沿系统回退链补齐，不会出现豆腐块；
 * - API 23–28：没有官方回退链可用，只能装完先跑 [UiFontGlyphCheck]，
 *   缺字则拒绝应用（见 [install]）。
 *
 * 与阅读正文字体互不干涉：正文字体走 `ReadBookConfig.textFont` + 自绘，不经过这里。
 */
object UiFontStore {

    private const val DIRECTORY = "uiFont"
    private const val MAX_BYTES = 64L * 1024 * 1024
    private val cache = LruCache<String, Typeface>(4)

    /** 已安装的自定义字体路径；空串表示未设置（回退到 [AppConfig.systemTypefaces]）。 */
    private val installedPath: String get() = AppConfig.uiFontPath

    /** 当前生效的界面字体；未设置自定义字体时返回 null。 */
    fun typeface(): Typeface? {
        val path = installedPath
        if (path.isBlank()) return null
        synchronized(cache) {
            cache.get(path)?.let { return it }
            val typeface = runCatching { decode(File(path)) }.getOrNull() ?: return null
            cache.put(path, typeface)
            return typeface
        }
    }

    /** 配置变动后丢弃缓存，下次读取重新解码。 */
    fun invalidate() {
        synchronized(cache) { cache.evictAll() }
    }

    /**
     * 安装字体：把 [source]（文件路径或 content:// URI）拷成私有副本并设为界面字体。
     *
     * @param displayName 展示名，同时决定副本扩展名
     * @return 成功为已安装文件，失败为异常
     */
    suspend fun install(
        context: Context,
        source: String,
        displayName: String,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val root = File(context.filesDir, DIRECTORY).apply { mkdirs() }
            val temp = File.createTempFile("install-", ".tmp", root)
            try {
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                openSource(context, source).use { input ->
                    temp.outputStream().use { output ->
                        var total = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            total += read
                            require(total <= MAX_BYTES) { "字体文件超过 64MB" }
                            output.write(buffer, 0, read)
                        }
                    }
                }
                temp.inputStream().use { input ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        digest.update(buffer, 0, read)
                    }
                }
                val ext = displayName.substringAfterLast('.', "")
                    .takeIf { it.isNotBlank() && it.length <= 4 } ?: "ttf"
                val fileName = digest.digest().joinToString("") { "%02x".format(it) } + ".$ext"
                val target = File(root, fileName)
                if (!target.exists() && !temp.renameTo(target)) {
                    // 副本已存在（内容相同）不算失败；改名失败才报错
                    if (!target.isFile) throw NoStackTraceException("无法保存字体")
                }
                val typeface = decode(target)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val missing = UiFontGlyphCheck.missingChars(typeface)
                    if (missing.isNotEmpty()) {
                        val sample = missing.take(30).joinToString("")
                        val reason = "字体缺少 %d 个常用字符，已取消应用：%s"
                            .format(missing.size, sample)
                        throw NoStackTraceException(reason)
                    }
                }
                AppConfig.uiFontPath = target.absolutePath
                AppConfig.uiFontName = displayName
                invalidate()
                target
            } finally {
                temp.delete()
            }
        }
    }

    /** 清除自定义界面字体，回退到系统字体选择。 */
    fun clear() {
        AppConfig.uiFontPath = ""
        AppConfig.uiFontName = ""
        invalidate()
    }

    private fun openSource(context: Context, source: String): InputStream {
        if (source.isContentScheme()) {
            val stream = context.contentResolver.openInputStream(source.toUri())
            return requireNotNull(stream) { "无法读取字体" }
        }
        val file = File(source)
        require(file.isFile) { "字体文件不存在" }
        return file.inputStream()
    }

    private fun decode(file: File): Typeface {
        require(file.isFile) { "字体文件不存在" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 官方回退链：自定义字体缺的字自动落到系统 sans-serif
            val family = FontFamily.Builder(Font.Builder(file).build()).build()
            return Typeface.CustomFallbackBuilder(family)
                .setSystemFallback("sans-serif")
                .build()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // createFromFile 对"存在但不是字体"的文件静默返回 DEFAULT，必须走 Builder
            return requireNotNull(Typeface.Builder(file).build()) { "字体文件无法解析" }
        }
        return Typeface.createFromFile(file)
    }
}

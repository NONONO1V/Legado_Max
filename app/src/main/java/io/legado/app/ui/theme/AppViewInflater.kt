package io.legado.app.ui.theme

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatViewInflater
import io.legado.app.R
import io.legado.app.lib.theme.applyUiTypeface
import io.legado.app.lib.theme.uiTypeface
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

/**
 * 界面字体第 1 层：XML 膨胀期注入。
 *
 * 由主题 `viewInflaterClass` 挂载（见 `res/values/styles.xml`），覆盖所有走 XML 的控件，
 * 包括 RecyclerView / Adapter 之后才膨胀的条目——这是"真全局"的主力：
 * 代码里 `new TextView()` 出来的控件不会经过这里，靠 [io.legado.app.base.BaseActivity]
 * 的递归补漏层兜。
 *
 * [androidx.annotation.Keep] 必须保留：主题里是字符串引用，不加会被混淆删掉。
 */
@Keep
class AppViewInflater : AppCompatViewInflater() {

    private val textConstructors = ConcurrentHashMap<String, Constructor<out TextView>>()

    private fun <T : TextView> T.withUiFont(context: Context, attrs: AttributeSet): T = apply {
        if (attrs.declaresMonospace(context)) {
            // 等宽是功能需求（规则搜索结果要对齐），不能被全局字体劫持；
            // 打上标记让递归补漏层也跳过它
            setTag(R.id.ui_monospace_lock, true)
            return@apply
        }
        applyUiTypeface(context.uiTypeface())
    }

    // 产出自卫型 UiFontTextView：菜单弹窗条目（ListMenuItemView.onFinishInflate）等
    // 会在膨胀后再 setTextAppearance 把字体冲回系统字体，见 UiFontTextView 类注释
    override fun createTextView(context: Context, attrs: AttributeSet) = UiFontTextView(context, attrs).withUiFont(context, attrs)

    override fun createButton(context: Context, attrs: AttributeSet) = super.createButton(context, attrs).withUiFont(context, attrs)

    override fun createEditText(context: Context, attrs: AttributeSet) = super.createEditText(context, attrs).withUiFont(context, attrs)

    override fun createCheckBox(context: Context, attrs: AttributeSet) = super.createCheckBox(context, attrs).withUiFont(context, attrs)

    override fun createRadioButton(context: Context, attrs: AttributeSet) = super.createRadioButton(context, attrs).withUiFont(context, attrs)

    override fun createCheckedTextView(context: Context, attrs: AttributeSet) = super.createCheckedTextView(context, attrs).withUiFont(context, attrs)

    override fun createAutoCompleteTextView(context: Context, attrs: AttributeSet) = super.createAutoCompleteTextView(context, attrs).withUiFont(context, attrs)

    override fun createMultiAutoCompleteTextView(context: Context, attrs: AttributeSet) = super.createMultiAutoCompleteTextView(context, attrs).withUiFont(context, attrs)

    override fun createToggleButton(context: Context, attrs: AttributeSet) = super.createToggleButton(context, attrs).withUiFont(context, attrs)

    /**
     * 自定义控件（XML 里写全限定名或 `class` 属性）也在这里造，
     * 否则这一类 TextView 子类会漏掉全局字体。
     *
     * `android:theme` 由 LayoutInflater 在调用工厂前就包进了 context，这里不需要再包。
     */
    override fun createView(context: Context, name: String, attrs: AttributeSet): View? {
        val className = if (name == "view") attrs.getAttributeValue(null, "class") else name
        if (className == null || '.' !in className) {
            return super.createView(context, name, attrs)
        }
        val constructor = textConstructors[className] ?: run {
            val viewClass = runCatching { context.classLoader.loadClass(className) }.getOrNull()
                ?: return super.createView(context, name, attrs)
            if (!TextView::class.java.isAssignableFrom(viewClass)) {
                return super.createView(context, name, attrs)
            }
            val textConstructor = try {
                viewClass.asSubclass(TextView::class.java)
                    .getConstructor(Context::class.java, AttributeSet::class.java)
            } catch (_: NoSuchMethodException) {
                return super.createView(context, name, attrs)
            }
            textConstructor.also { textConstructors[className] = it }
        }
        // android:theme 已由 LayoutInflater 在调用工厂前包好 context，这里直接用
        return constructor.newInstance(context, attrs).withUiFont(context, attrs)
    }

    private fun AttributeSet.declaresMonospace(context: Context): Boolean {
        fun isMonospace(raw: String?): Boolean {
            val value = when {
                raw == null -> return false
                raw.startsWith("@") -> raw.substring(1).toIntOrNull()
                    ?.let { runCatching { context.resources.getString(it) }.getOrNull() }
                    ?: return false
                else -> raw
            }
            // "3" 是 android:typeface 枚举里的 monospace
            return value == "monospace" || value == "3"
        }
        return isMonospace(getAttributeValue(ANDROID_NS, "fontFamily")) ||
            isMonospace(getAttributeValue(ANDROID_NS, "typeface"))
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    }
}

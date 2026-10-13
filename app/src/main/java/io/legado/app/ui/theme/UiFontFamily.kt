package io.legado.app.ui.theme

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

/**
 * 把已加载好的 [Typeface] 包成 Compose 的 [FontFamily]。
 *
 * 自定义字体是外部 ttf/otf，Compose 没有现成的 Font 包装，这里用 [AndroidFont] +
 * [FontLoadingStrategy.Blocking] 把实例直接喂进去，不再走一遍异步解码。
 */
internal fun uiFontFamily(typeface: Typeface): FontFamily = when (typeface) {
    Typeface.DEFAULT, Typeface.SANS_SERIF -> FontFamily.Default
    Typeface.SERIF -> FontFamily.Serif
    Typeface.MONOSPACE -> FontFamily.Monospace
    else -> FontFamily(PreloadedUiFont(typeface))
}

private class PreloadedUiFont(val typeface: Typeface) : AndroidFont(
    loadingStrategy = FontLoadingStrategy.Blocking,
    typefaceLoader = PreloadedTypefaceLoader,
    variationSettings = FontVariation.Settings(),
) {
    override val weight: FontWeight = FontWeight.Normal
    override val style: FontStyle = FontStyle.Normal

    override fun equals(other: Any?): Boolean =
        other is PreloadedUiFont && typeface == other.typeface

    override fun hashCode(): Int = typeface.hashCode()
}

private object PreloadedTypefaceLoader : AndroidFont.TypefaceLoader {
    override fun loadBlocking(context: Context, font: AndroidFont): Typeface =
        (font as PreloadedUiFont).typeface

    override suspend fun awaitLoad(context: Context, font: AndroidFont): Typeface =
        loadBlocking(context, font)
}

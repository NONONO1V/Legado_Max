package io.legado.app.lib.theme

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.UiFontStore
import io.legado.app.utils.dpToPx

/**
 * 当前界面字体：自定义字体优先，没有自定义字体时按「系统字体」设置返回。
 *
 * 范围是**整个界面**（含标题栏、区块标题），不含阅读正文——正文走
 * `ChapterProvider.getTypeface()` + `ReadBookConfig.textFont`，是自绘且另有配置。
 */
fun Context.uiTypeface(): Typeface {
    return UiFontStore.typeface() ?: when (AppConfig.systemTypefaces) {
        1 -> Typeface.SERIF
        2 -> Typeface.MONOSPACE
        else -> Typeface.SANS_SERIF
    }
}

/**
 * 给单个 TextView 套用界面字体，保留原有字重（bold / italic 不被吃掉）。
 *
 * 标记了 `ui_monospace_lock` 的控件（XML 里声明了等宽的，如规则搜索结果）跳过。
 */
fun TextView.applyUiTypeface(typeface: Typeface = context.uiTypeface()) {
    if (getTag(R.id.ui_monospace_lock) == true) return
    setTypeface(typeface, this.typeface?.style ?: Typeface.NORMAL)
}

/**
 * 递归设置整棵 View 树的字体。
 *
 * 这是"真全局"的第 3 层补漏：XML 膨胀期注入（AppViewInflater）覆盖不到
 * 代码里 `new TextView()` 出来的控件，也不覆盖在补漏之后才被 addView 的控件，
 * 因此对已挂载的 View 树再走一遍。不跳过任何控件。
 */
fun View.applyUiTypefaceDeep(typeface: Typeface = context.uiTypeface()) {
    when (this) {
        is TextView -> applyUiTypeface(typeface)
        is ViewGroup -> {
            for (index in 0 until childCount) {
                getChildAt(index).applyUiTypefaceDeep(typeface)
            }
        }
    }
}

/** 应用标签样式：小字号、次要色 */
fun TextView.applyUiLabelStyle(context: Context) {
    applyUiTypeface(context.uiTypeface())
    textSize = 14f
    setTextColor(ContextCompat.getColor(context, R.color.secondaryText))
}

/** 应用区块标题样式：中字号、主色 */
fun TextView.applyUiSectionTitleStyle(context: Context) {
    applyUiTypeface(context.uiTypeface())
    textSize = 15f
    setTextColor(ContextCompat.getColor(context, R.color.primaryText))
}

/** 应用输入框样式：自动设置字号、行数限制、内边距 */
fun EditText.applyUiInputStyle(context: Context, minLines: Int = 1) {
    applyUiTypeface(context.uiTypeface())
    textSize = 15f
    this.minLines = minLines
    maxLines = if (minLines > 1) 8 else 2
    setSingleLine(minLines == 1)
    minHeight = if (minLines > 1) 92.dpToPx() else 44.dpToPx()
    setTextColor(ContextCompat.getColor(context, R.color.primaryText))
    setHintTextColor(ContextCompat.getColor(context, R.color.secondaryText))
    val horizontal = 12.dpToPx()
    val vertical = if (minLines > 1) 10.dpToPx() else 8.dpToPx()
    setPadding(horizontal, vertical, horizontal, vertical)
}

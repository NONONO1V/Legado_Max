package io.legado.app.ui.theme

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatTextView
import io.legado.app.lib.theme.applyUiTypeface
import io.legado.app.lib.theme.uiTypeface

/**
 * 界面字体第 1 层的自卫型 TextView（由 [AppViewInflater] 在膨胀期产出）。
 *
 * 存在一类控件会在膨胀完成**之后**再对内部 TextView 调 `setTextAppearance` 重设样式，
 * 而 TextAppearance 自带的 `fontFamily` 会把膨胀期设好的界面字体冲回系统字体：
 *
 * - 菜单弹窗条目 `ListMenuItemView.onFinishInflate()` 对标题补一层
 *   `itemTextAppearance`——三点溢出菜单、PopupMenu、子菜单全走这里；
 * - 条目布局自身的 `android:textAppearance` 若在运行时被代码再次赋值同理。
 *
 * 因此在 `setTextAppearance` 之后把界面字体补回去。
 * 等宽锁定（`ui_monospace_lock`）由 [applyUiTypeface] 内部判断，同样生效。
 */
@Keep
class UiFontTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun setTextAppearance(themeContext: Context, resId: Int) {
        super.setTextAppearance(themeContext, resId)
        applyUiTypeface(context.uiTypeface())
    }
}

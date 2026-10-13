package io.legado.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.legado.app.lib.theme.uiTypeface
import splitties.init.appCtx

/**
 * 应用排版档位，经 [LegadoTheme] 注入 `MaterialTheme.typography`（规范见 theme-styles.md §7.4）。
 *
 * 字号与权重沿用 Material3 默认档位，本项目不另设字号体系。这里只织入一件事：
 * 用户「界面字体」设置（自定义字体优先，否则 [io.legado.app.help.config.AppConfig.systemTypefaces]
 * 0 默认 / 1 衬线 / 2 等宽）——**15 个档位全部注入**，与 View 侧
 * [io.legado.app.lib.theme.applyUiTypefaceDeep] 一样不再区分标题/正文角色。
 *
 * 设置没有可观察源：改设置后靠主题应用/页面重建带来的重组读到新值，
 * 与 View 侧需重新遍历 View 树生效是同一限制。
 */
@Composable
fun rememberAppTypography(): Typography {
    val typeface = appCtx.uiTypeface()
    return remember(typeface) {
        val fontFamily = uiFontFamily(typeface)
        val m3 = Typography()
        m3.copy(
            displayLarge = m3.displayLarge.copy(fontFamily = fontFamily),
            displayMedium = m3.displayMedium.copy(fontFamily = fontFamily),
            displaySmall = m3.displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = m3.headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = m3.headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = m3.headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = m3.titleLarge.copy(fontFamily = fontFamily),
            titleMedium = m3.titleMedium.copy(fontFamily = fontFamily),
            titleSmall = m3.titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = m3.bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = m3.bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = m3.bodySmall.copy(fontFamily = fontFamily),
            labelLarge = m3.labelLarge.copy(fontFamily = fontFamily),
            labelMedium = m3.labelMedium.copy(fontFamily = fontFamily),
            labelSmall = m3.labelSmall.copy(fontFamily = fontFamily),
        )
    }
}

package us.webmy.core.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import us.webmy.core.WebMY
import us.webmy.core.internal.theme.LocalWebmySpacings
import us.webmy.core.internal.theme.LocalWebmyTypography
import us.webmy.core.internal.theme.RealWebmySpacings
import us.webmy.core.internal.theme.RealWebmyTypography
import us.webmy.core.theme.WebmySpacings
import us.webmy.core.theme.WebmyTypography
import us.webmy.core.internal.theme.LocalColorsPalette
import us.webmy.core.theme.ColorsPalette
import us.webmy.core.internal.theme.toMaterialColorScheme
import us.webmy.core.internal.theme.materialShapes

@Composable
fun AppTheme(
    controller: WebmyThemeController = WebMY.theme,
    content: @Composable () -> Unit,
) {
    val themeId by controller.theme.collectAsState()

    val colors = remember(themeId) { controller.palette(themeId) }
    val isDark = remember(themeId) { controller.isDark(themeId) }

    AppTheme(colors = colors, isDark = isDark, content = content)
}

@Composable
fun AppTheme(
    colors: ColorsPalette,
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    val typography = remember { RealWebmyTypography() }
    val spacings = remember { RealWebmySpacings() }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = view.context.findActivity()?.window
        if (window != null) {
            SideEffect {
                val insets = WindowCompat.getInsetsController(window, view)
                insets.isAppearanceLightStatusBars = !isDark
                insets.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val textSelectionColors = remember(colors) {
        TextSelectionColors(
            handleColor = colors.textAndIconsTertiary,
            backgroundColor = colors.textAndIconsPrimary.copy(alpha = 0.34f),
        )
    }

    val rippleIndication = ripple(color = colors.fill24)
    val materialColorScheme = remember(colors) { colors.toMaterialColorScheme() }
    val materialTypography = remember(typography) { typography.toMaterialTypography() }
    val materialShapes = materialShapes()

    MaterialTheme(
        colorScheme = materialColorScheme,
        typography = materialTypography,
        shapes = materialShapes,
    ) {
        CompositionLocalProvider(
            LocalColorsPalette provides colors,
            LocalWebmyTypography provides typography,
            LocalWebmySpacings provides spacings,
            LocalIndication provides rippleIndication,
            LocalContentColor provides colors.textAndIconsPrimary,
            LocalTextStyle provides typography.bodyM,
            LocalTextSelectionColors provides textSelectionColors,
        ) {
            content()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object WebmyTheme {
    val colors: ColorsPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalColorsPalette.current

    val typography: WebmyTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalWebmyTypography.current

    val spacings: WebmySpacings
        @Composable
        @ReadOnlyComposable
        get() = LocalWebmySpacings.current
}

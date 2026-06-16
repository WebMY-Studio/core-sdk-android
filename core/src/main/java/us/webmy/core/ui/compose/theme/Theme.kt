package us.webmy.core.ui.compose.theme

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
import org.koin.compose.koinInject
import us.webmy.core.ui.compose.configs.LocalWebmySpacings
import us.webmy.core.ui.compose.configs.LocalWebmyTypography
import us.webmy.core.ui.compose.configs.RealWebmySpacings
import us.webmy.core.ui.compose.configs.RealWebmyTypography
import us.webmy.core.ui.compose.configs.WebmySpacings
import us.webmy.core.ui.compose.configs.WebmyTypography
import us.webmy.core.ui.compose.configs.colors.LocalColorsPalette
import us.webmy.core.ui.compose.configs.colors.palettes.ColorsPalette
import us.webmy.core.ui.compose.configs.colors.palettes.toMaterialColorScheme
import us.webmy.core.ui.compose.configs.materialShapes

@Composable
fun AppTheme(
    controller: WebmyThemeController = koinInject(),
    content: @Composable () -> Unit,
) {
    val themeId by controller.theme.collectAsState()

    val colors = remember(themeId) { controller.palette(themeId) }
    val isDark = remember(themeId) { controller.spec(themeId).isDark }

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

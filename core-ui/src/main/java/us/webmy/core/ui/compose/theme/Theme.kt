package us.webmy.core.ui.compose.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import us.webmy.core.ui.compose.configs.LocalWebmySpacings
import us.webmy.core.ui.compose.configs.LocalWebmyTypography
import us.webmy.core.ui.compose.configs.RealWebmySpacings
import us.webmy.core.ui.compose.configs.RealWebmyTypography
import us.webmy.core.ui.compose.configs.WebmySpacings
import us.webmy.core.ui.compose.configs.WebmyTypography
import us.webmy.core.ui.compose.configs.colors.LocalColorsPalette
import us.webmy.core.ui.compose.configs.colors.palettes.LightColorsPalette
import us.webmy.core.ui.compose.configs.colors.palettes.ColorsPalette
import us.webmy.core.ui.compose.configs.materialShapes

private val LightColorsPalette = LightColorsPalette()

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colors = LightColorsPalette
    val typography = remember { RealWebmyTypography() }
    val spacings = remember { RealWebmySpacings() }

    val textSelectionColors = remember(colors) {
        TextSelectionColors(
            handleColor = colors.textAndIconsTertiary,
            backgroundColor = colors.textAndIconsPrimary.copy(alpha = 0.34f)
        )
    }

    val rippleColor = remember { Color(0x3DFFFFFF) }
    val rippleIndication = ripple(color = rippleColor)
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

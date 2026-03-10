package us.webmy.core_sdk_compose.theme

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
import androidx.compose.ui.graphics.Color
import us.webmy.core_sdk_compose.configs.LocalWebmySpacings
import us.webmy.core_sdk_compose.configs.LocalWebmyTypography
import us.webmy.core_sdk_compose.configs.RealWebmySpacings
import us.webmy.core_sdk_compose.configs.RealWebmyTypography
import us.webmy.core_sdk_compose.configs.WebmySpacings
import us.webmy.core_sdk_compose.configs.WebmyTypography
import us.webmy.core_sdk_compose.configs.colors.LocalWebmyColors
import us.webmy.core_sdk_compose.configs.colors.palettes.DarkColorsPalette
import us.webmy.core_sdk_compose.configs.colors.palettes.WebmyColorsPalette
import us.webmy.core_sdk_compose.configs.materialShapes

private val DarkColorsPalette = DarkColorsPalette()

@Composable
fun WebmyTheme(
    content: @Composable () -> Unit
) {
    val colors = DarkColorsPalette
    val typography = RealWebmyTypography()

    val textSelectionColors = TextSelectionColors(
        handleColor = colors.textAndIconsTertiary,
        backgroundColor = colors.textAndIconsPrimary.copy(alpha = 0.34f)
    )

    val rippleColor = Color(0x3DFFFFFF)

    MaterialTheme(
        colorScheme = colors.toMaterialColorScheme(),
        typography = typography.toMaterialTypography(),
        shapes = materialShapes()
    ) {
        CompositionLocalProvider(
            LocalWebmyColors provides colors,
            LocalWebmyTypography provides RealWebmyTypography(),
            LocalWebmySpacings provides RealWebmySpacings(),
            LocalIndication provides ripple(color = rippleColor),
            LocalContentColor provides colors.textAndIconsPrimary,
            LocalTextStyle provides typography.bodyM,
            LocalTextSelectionColors provides textSelectionColors
        ) {
            content()
        }
    }
}

object WebmyTheme {
    val colors: WebmyColorsPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalWebmyColors.current

    val typography: WebmyTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalWebmyTypography.current

    val spacings: WebmySpacings
        @Composable
        @ReadOnlyComposable
        get() = LocalWebmySpacings.current
}

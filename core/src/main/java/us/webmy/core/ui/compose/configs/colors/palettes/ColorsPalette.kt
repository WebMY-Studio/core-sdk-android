package us.webmy.core.ui.compose.configs.colors.palettes

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

abstract class ColorsPalette {
    abstract val backgroundSystem: Color
    abstract val backgroundPrimary: Color
    abstract val backgroundSecondary: Color
    abstract val backgroundTertiary: Color

    abstract val backgroundInverse: Color

    abstract val textAndIconsPrimary: Color
    abstract val textAndIconsSecondary: Color
    abstract val textAndIconsTertiary: Color
    abstract val textAndIconsInversePrimary: Color
    abstract val textAndIconsDisabled: Color

    abstract val appliedOverlay: Color
    abstract val appliedHover: Color
    abstract val appliedStroke: Color
    abstract val appliedSeparator: Color
    abstract val appliedButtonText: Color

    abstract val indicatorDisabled: Color

    abstract val fill2: Color
    abstract val fill6: Color
    abstract val fill8: Color
    abstract val fill12: Color
    abstract val fill18: Color
    abstract val fill24: Color
    abstract val fill30: Color
    abstract val fill48: Color
    abstract val fill70: Color
    abstract val fill100: Color

    abstract val fillDark30: Color
    abstract val fillDark45: Color
    abstract val fillDark66: Color
    abstract val fillDark100: Color
    abstract val success: Color
    abstract val error: Color
    abstract val warning: Color

    fun toMaterialColorScheme(): ColorScheme = ColorScheme(
        primary = backgroundPrimary,
        onPrimary = textAndIconsPrimary,
        primaryContainer = backgroundTertiary,
        onPrimaryContainer = textAndIconsPrimary,
        inversePrimary = textAndIconsPrimary,
        secondary = backgroundSecondary,
        onSecondary = textAndIconsSecondary,
        secondaryContainer = backgroundTertiary,
        onSecondaryContainer = textAndIconsPrimary,
        tertiary = backgroundTertiary,
        onTertiary = textAndIconsTertiary,
        tertiaryContainer = backgroundSecondary,
        onTertiaryContainer = textAndIconsTertiary,
        background = backgroundPrimary,
        onBackground = textAndIconsPrimary,
        surface = backgroundPrimary,
        onSurface = textAndIconsPrimary,
        surfaceVariant = backgroundSecondary,
        onSurfaceVariant = textAndIconsPrimary,
        surfaceTint = textAndIconsTertiary,
        inverseSurface = textAndIconsSecondary,
        inverseOnSurface = backgroundPrimary,
        error = error,
        onError = error,
        errorContainer = error,
        onErrorContainer = error,
        outline = appliedStroke,
        outlineVariant = appliedSeparator,
        scrim = fill30,
        surfaceBright = backgroundPrimary,
        surfaceDim = backgroundPrimary,
        surfaceContainer = textAndIconsPrimary,
        surfaceContainerHigh = textAndIconsPrimary,
        surfaceContainerHighest = textAndIconsPrimary,
        surfaceContainerLow = textAndIconsPrimary,
        surfaceContainerLowest = textAndIconsPrimary
    )
}

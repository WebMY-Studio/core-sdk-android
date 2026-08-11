package us.webmy.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

abstract class WebmyTypography {
    abstract val titleXXXXL: TextStyle
    abstract val titleXXXL: TextStyle
    abstract val titleXXL: TextStyle
    abstract val titleXL: TextStyle
    abstract val titleL: TextStyle
    abstract val titleM: TextStyle
    abstract val titleSMedium: TextStyle
    abstract val titleSSemiBold: TextStyle

    abstract val headline: TextStyle

    abstract val bodyM: TextStyle
    abstract val bodyS: TextStyle

    abstract val bodySSemiBold: TextStyle

    abstract val caption1: TextStyle
    abstract val caption1SemiBold: TextStyle
    abstract val caption2: TextStyle

    fun toMaterialTypography(): Typography = Typography(
        displayLarge = titleXL,
        displayMedium = titleM,
        displaySmall = titleSSemiBold,
        headlineLarge = headline,
        headlineMedium = headline,
        headlineSmall = headline,
        titleLarge = titleXL,
        titleMedium = titleM,
        titleSmall = titleSMedium,
        bodyLarge = bodyM,
        bodyMedium = bodyM,
        bodySmall = bodyS,
        labelLarge = caption1,
        labelMedium = caption1,
        labelSmall = caption2
    )
}

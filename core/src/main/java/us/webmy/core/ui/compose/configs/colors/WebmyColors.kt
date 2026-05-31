package us.webmy.core.ui.compose.configs.colors

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import us.webmy.core.ui.compose.configs.colors.palettes.ColorsPalette
import us.webmy.core.ui.compose.utils.noLocalProvidedFor

internal val LocalColorsPalette = staticCompositionLocalOf<ColorsPalette> {
    noLocalProvidedFor("ColorsPalette")
}

object StableColors {
    val Gray = Color(0xFFF3F3F2)
    val Gray100 = Color(0xFFA4A1A1)
    val Gray200 = Color(0xFF1F1F1F)
    val Gray300 = Color(0xFF141414)
    val BrandPinkBackground = Color(0xFF2D1B22)
    val BrandGreen = Color(0xFF56F39A)
    val BrandLime = Color(0xFFD3FF33)
    val BrandLimeBackground = Color(0x14FFF626)
    val BrandLimeBackgroundDark = Color(0x0AD3FF33)
    val Yellow = Color(0xFFFFB300)

    val BrandPink = Color(0xFFE6007A)
    val BrandCyan = Color(0xFF009393)
    val BrandBlue = Color(0xFF2775CA)
}

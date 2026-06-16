package us.webmy.core.ui.compose.configs.colors.palettes

import androidx.compose.ui.graphics.Color

class LightColorsPalette : ColorsPalette() {
    // backgroundPrimary: #FBFBFB, backgroundSecondary: #080E0E0F
    override val backgroundSystem = Color(0xFFFBFBFB)
    override val backgroundPrimary = Color(0xFFFBFBFB)
    override val backgroundSecondary = Color(0x080E0E0F)
    override val backgroundTertiary = Color(0xFFF3F3F2)
    override val backgroundInverse = Color(0xFF0E0E0F)

    // textAndIconsPrimary: #0E0E0F, textAndIconsSecondary: #9F9F9F, textAndIconsPrimaryInverse: #FFFFFF, textDisabled: #DDDDDD
    override val textAndIconsPrimary = Color(0xFF0E0E0F)
    override val textAndIconsSecondary = Color(0xFF9F9F9F)
    override val textAndIconsTertiary = Color(0xFF6E6E6F)
    override val textAndIconsInversePrimary = Color(0xFFFFFFFF)
    override val textAndIconsDisabled = Color(0xFFDDDDDD)

    // borderPrimary: #0F0E0E0F, borderSecondary: #080E0E0F
    override val appliedOverlay = Color(0x80000000)
    override val appliedHover = Color(0x080E0E0F)
    override val appliedButtonText = Color(0xFF0E0E0F)
    override val appliedStroke = Color(0x0F0E0E0F)
    override val appliedSeparator = Color(0x080E0E0F)

    override val indicatorDisabled = Color(0xFFA4A1A1)

    // Fill — opacity variants of #0E0E0F on light background
    override val fill2 = Color(0x050E0E0F)
    override val fill6 = Color(0x0F0E0E0F)
    override val fill8 = Color(0x140E0E0F)
    override val fill12 = Color(0x1F0E0E0F)
    override val fill18 = Color(0x2E0E0E0F)
    override val fill24 = Color(0x3D0E0E0F)
    override val fill30 = Color(0x4D0E0E0F)
    override val fill48 = Color(0x7A0E0E0F)
    override val fill70 = Color(0xB30E0E0F)
    override val fill100 = Color(0xFF0E0E0F)

    override val fillDark30 = Color(0x4D000000)
    override val fillDark45 = Color(0x73000000)
    override val fillDark66 = Color(0xA8000000)
    override val fillDark100 = Color(0xFF000000)

    // error: #D32F2F
    override val success = Color(0xFF35C759)
    override val error = Color(0xFFD32F2F)
    override val warning = Color(0xFFFFB300)
}

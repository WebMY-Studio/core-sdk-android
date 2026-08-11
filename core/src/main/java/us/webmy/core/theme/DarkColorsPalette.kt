package us.webmy.core.theme

import androidx.compose.ui.graphics.Color

class DarkColorsPalette : ColorsPalette() {
    override val backgroundSystem = Color(0xFF0E0E0F)
    override val backgroundPrimary = Color(0xFF0E0E0F)
    override val backgroundSecondary = Color(0x08FFFFFF)
    override val backgroundTertiary = Color(0xFF1A1A1C)
    override val backgroundInverse = Color(0xFFFBFBFB)

    // textAndIconsPrimary: #FFFFFF, textAndIconsSecondary: #6E6E6F, textAndIconsPrimaryInverse: #0E0E0F, textDisabled: #303031
    override val textAndIconsPrimary = Color(0xFFFFFFFF)
    override val textAndIconsSecondary = Color(0xFF6E6E6F)
    override val textAndIconsTertiary = Color(0xFF9F9F9F)
    override val textAndIconsInversePrimary = Color(0xFF0E0E0F)
    override val textAndIconsDisabled = Color(0xFF303031)

    // borderPrimary: #0FFFFFFF, borderSecondary: #08FFFFFF
    override val appliedOverlay = Color(0x80000000)
    override val appliedHover = Color(0x08FFFFFF)
    override val appliedButtonText = Color(0xFFFFFFFF)
    override val appliedStroke = Color(0x0FFFFFFF)
    override val appliedSeparator = Color(0x08FFFFFF)

    override val indicatorDisabled = Color(0xFF5E5E60)

    // Fill — opacity variants of #FFFFFF on dark background
    override val fill2 = Color(0x05FFFFFF)
    override val fill6 = Color(0x0FFFFFFF)
    override val fill8 = Color(0x14FFFFFF)
    override val fill12 = Color(0x1FFFFFFF)
    override val fill18 = Color(0x2EFFFFFF)
    override val fill24 = Color(0x3DFFFFFF)
    override val fill30 = Color(0x4DFFFFFF)
    override val fill48 = Color(0x7AFFFFFF)
    override val fill70 = Color(0xB3FFFFFF)
    override val fill100 = Color(0xFFFFFFFF)

    override val fillDark30 = Color(0x4D000000)
    override val fillDark45 = Color(0x73000000)
    override val fillDark66 = Color(0xA8000000)
    override val fillDark100 = Color(0xFF000000)

    // error: #D32F2F
    override val success = Color(0xFF35C759)
    override val error = Color(0xFFD32F2F)
    override val warning = Color(0xFFFFB300)
}

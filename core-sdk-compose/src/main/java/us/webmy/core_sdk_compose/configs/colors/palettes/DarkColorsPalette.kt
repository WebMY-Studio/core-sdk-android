package us.webmy.core_sdk_compose.configs.colors.palettes

import androidx.compose.ui.graphics.Color

class DarkColorsPalette : WebmyColorsPalette() {
    override val backgroundSystem = Color(0xFF000000)
    override val backgroundPrimary = Color(0xFF000000)
    override val backgroundSecondary = Color(0xFF141414)
    override val backgroundTertiary = Color(0xFF1F1F1F)
    override val backgroundInverse = Color(0xFFFFFFFF)

    override val textAndIconsPrimary = Color(0xFFFFFFFF)
    override val textAndIconsSecondary = Color(0xB0FFFFFF)
    override val textAndIconsTertiary = Color(0x7AFFFFFF)
    override val textAndIconsInversePrimary = Color(0xFF000000)
    override val textAndIconsDisabled = Color(0x45FFFFFF)

    override val appliedOverlay = Color(0xB3000000)
    override val appliedHover = Color(0x0FFFFFFF)
    override val appliedButtonText = Color(0xFF000000)
    override val appliedStroke = Color(0x1FFFFFFF)
    override val appliedSeparator = Color(0x14FFFFFF)

    override val indicatorDisabled = Color(0xFF4E4E4E)

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

    override val success = Color(0xFF35C759)
    override val error = Color(0xFFFF3123)
    override val warning: Color = Color(0xFFF272B6)
}

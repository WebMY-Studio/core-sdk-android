package us.webmy.coresdkdemo

import androidx.compose.ui.graphics.Color
import us.webmy.core.theme.ColorsPalette

class AccentColorsPalette : ColorsPalette() {
    override val backgroundSystem = Color(0xFF1A1230)
    override val backgroundPrimary = Color(0xFF1A1230)
    override val backgroundSecondary = Color(0x0AFFFFFF)
    override val backgroundTertiary = Color(0xFF272042)
    override val backgroundInverse = Color(0xFFEDE9FB)

    override val textAndIconsPrimary = Color(0xFFEDE9FB)
    override val textAndIconsSecondary = Color(0xFFB0A8C8)
    override val textAndIconsTertiary = Color(0xFF8E86A8)
    override val textAndIconsInversePrimary = Color(0xFF1A1230)
    override val textAndIconsDisabled = Color(0xFF4A4366)

    override val appliedOverlay = Color(0x80000000)
    override val appliedHover = Color(0x0AFFFFFF)
    override val appliedButtonText = Color(0xFFEDE9FB)
    override val appliedStroke = Color(0x14FFFFFF)
    override val appliedSeparator = Color(0x0FFFFFFF)

    override val indicatorDisabled = Color(0xFF6E6788)

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
    override val error = Color(0xFFFF6B6B)
    override val warning = Color(0xFFFFB300)
}

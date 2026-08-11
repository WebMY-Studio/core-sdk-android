package us.webmy.core.theme

import us.webmy.core.theme.BuildInThemeIds
import us.webmy.core.theme.ThemeId
import us.webmy.core.theme.ColorsPalette
import us.webmy.core.theme.DarkColorsPalette
import us.webmy.core.theme.LightColorsPalette

class ThemePalette(
    val id: ThemeId,
    val isDark: Boolean,
    val palette: ColorsPalette,
) {
    companion object {
        val LIGHT = ThemePalette(BuildInThemeIds.LIGHT, isDark = false, palette = LightColorsPalette())
        val DARK = ThemePalette(BuildInThemeIds.DARK, isDark = true, palette = DarkColorsPalette())
        val DEFAULT = LIGHT
    }
}

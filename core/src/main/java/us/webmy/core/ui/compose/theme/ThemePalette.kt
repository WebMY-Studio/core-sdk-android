package us.webmy.core.ui.compose.theme

import us.webmy.core.domain.model.BuildInThemeIds
import us.webmy.core.domain.model.ThemeId
import us.webmy.core.ui.compose.configs.colors.palettes.ColorsPalette
import us.webmy.core.ui.compose.configs.colors.palettes.DarkColorsPalette
import us.webmy.core.ui.compose.configs.colors.palettes.LightColorsPalette

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

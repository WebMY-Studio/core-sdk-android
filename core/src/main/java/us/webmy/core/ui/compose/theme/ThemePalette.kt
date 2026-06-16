package us.webmy.core.ui.compose.theme

import us.webmy.core.R
import us.webmy.core.ui.compose.configs.colors.palettes.ColorsPalette
import us.webmy.core.domain.model.ThemeSpec
import us.webmy.core.ui.compose.configs.colors.palettes.DarkColorsPalette
import us.webmy.core.ui.compose.configs.colors.palettes.LightColorsPalette

class ThemePalette(
    val spec: ThemeSpec,
    val palette: ColorsPalette,
) {
    companion object {
        val LIGHT = ThemePalette(
            spec = ThemeSpec("light", isDark = false, nameRes = R.string.webmy_theme_light),
            palette = LightColorsPalette(),
        )

        val DARK = ThemePalette(
            spec = ThemeSpec("dark", isDark = true, nameRes = R.string.webmy_theme_dark),
            palette = DarkColorsPalette(),
        )

        val DEFAULT = LIGHT
    }
}

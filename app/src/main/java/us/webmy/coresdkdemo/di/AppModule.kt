package us.webmy.coresdkdemo.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import us.webmy.core.domain.model.ThemeSpec
import us.webmy.core.ui.compose.theme.ThemePalette
import us.webmy.coresdkdemo.AccentColorsPalette
import us.webmy.coresdkdemo.R
import us.webmy.coresdkdemo.SettingsArgs
import us.webmy.coresdkdemo.SettingsViewModel

val appModule = module {
    viewModel { (args: SettingsArgs) -> SettingsViewModel(args) }

    val themeId = "accent"
    single(named(themeId)) {
        ThemePalette(
            spec = ThemeSpec(id = themeId, isDark = true, nameRes = R.string.theme_accent),
            palette = AccentColorsPalette(),
        )
    }
}

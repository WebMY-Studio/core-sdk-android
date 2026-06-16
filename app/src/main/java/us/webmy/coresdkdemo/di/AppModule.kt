package us.webmy.coresdkdemo.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import us.webmy.core.ui.compose.theme.ThemePalette
import us.webmy.coresdkdemo.AccentColorsPalette
import us.webmy.coresdkdemo.ThemeIds
import us.webmy.coresdkdemo.SettingsArgs
import us.webmy.coresdkdemo.SettingsViewModel

val appModule = module {
    viewModel { (args: SettingsArgs) -> SettingsViewModel(args) }

    single(named(ThemeIds.ACCENT)) {
        ThemePalette(
            id = ThemeIds.ACCENT,
            isDark = true,
            palette = AccentColorsPalette(),
        )
    }
}

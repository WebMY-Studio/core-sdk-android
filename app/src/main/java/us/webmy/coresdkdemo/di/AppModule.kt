package us.webmy.coresdkdemo.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import us.webmy.core.ui.compose.theme.ThemePalette
import us.webmy.coresdkdemo.AccentColorsPalette
import us.webmy.coresdkdemo.ThemeIds

val appModule = module {
    single(named(ThemeIds.ACCENT)) {
        ThemePalette(
            id = ThemeIds.ACCENT,
            isDark = true,
            palette = AccentColorsPalette(),
        )
    }
}

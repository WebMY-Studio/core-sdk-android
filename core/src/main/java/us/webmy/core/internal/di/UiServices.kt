package us.webmy.core.internal.di

import us.webmy.core.prefs.OnboardingShownPreferences
import us.webmy.core.internal.theme.ThemePreferences
import us.webmy.core.internal.theme.ThemeRepository
import us.webmy.core.internal.theme.ThemeRepositoryImpl
import us.webmy.core.theme.ThemePalette
import us.webmy.core.theme.WebmyThemeController
import us.webmy.core.navigation.Router
import us.webmy.core.internal.navigation.WebmyRouter
import us.webmy.core.internal.navigation.SheetController

internal fun registerUiServices(extraPalettes: List<ThemePalette>) = with(ServiceRegistry) {
    register { SheetController() }
    register { OnboardingShownPreferences(resolve()) }

    register { ThemePreferences(resolve()) }
    register<ThemeRepository> { ThemeRepositoryImpl(resolve<ThemePreferences>()) }

    register {
        WebmyThemeController(
            repository = resolve(),
            palettes = listOf(ThemePalette.LIGHT, ThemePalette.DARK) + extraPalettes,
        )
    }

    register<Router> {
        WebmyRouter(
            activityProvider = resolve(),
            biometricsService = resolve(),
            sheetController = resolve(),
        )
    }
}

package us.webmy.core.ui.di

import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import us.webmy.core.WebMY
import us.webmy.core.data.prefs.OnboardingShownPreferences
import us.webmy.core.data.prefs.ThemePreferences
import us.webmy.core.data.repo.ThemeRepository
import us.webmy.core.data.repo.ThemeRepositoryImpl
import us.webmy.core.ui.compose.theme.ThemePalette
import us.webmy.core.ui.compose.theme.WebmyThemeController
import us.webmy.core.ui.presentation.base.navigator.Router
import us.webmy.core.ui.presentation.base.navigator.WebmyRouter
import us.webmy.core.ui.single.SheetController

internal fun uiModule(): Module = module {
    single { SheetController() }
    single { OnboardingShownPreferences(get()) }

    single { ThemePreferences(get()) }
    single { ThemeRepositoryImpl(get()) } bind ThemeRepository::class

    single(named(ThemePalette.LIGHT.spec.id)) { ThemePalette.LIGHT }

    single(named(ThemePalette.DARK.spec.id)) { ThemePalette.DARK }

    single { WebmyThemeController(repository = get(), themes = getAll<ThemePalette>()) }

    single<Router> {
        WebmyRouter(
            activityProvider = get(),
            biometricsService = get(),
            sheetController = get(),
        )
    }
}

fun WebMY.installUi() {
    loadKoinModules(uiModule())
}

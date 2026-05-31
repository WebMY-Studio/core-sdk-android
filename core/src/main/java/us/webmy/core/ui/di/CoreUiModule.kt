package us.webmy.core.ui.di

import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import us.webmy.core.WebMY
import us.webmy.core.ui.data.prefs.OnboardingShownPreferences
import us.webmy.core.ui.presentation.base.navigator.Router
import us.webmy.core.ui.presentation.base.navigator.WebmyRouter
import us.webmy.core.ui.single.SheetController

internal fun uiModule(): Module = module {
    single { SheetController() }
    single { OnboardingShownPreferences(get()) }
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

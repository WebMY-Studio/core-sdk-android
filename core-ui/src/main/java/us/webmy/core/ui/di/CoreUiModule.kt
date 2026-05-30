package us.webmy.core.ui.di

import org.koin.core.module.Module
import org.koin.dsl.module
import us.webmy.core.ui.presentation.base.navigator.AdNavigationHandler
import us.webmy.core.ui.presentation.base.navigator.Navigator
import us.webmy.core.ui.presentation.base.navigator.PurchaseNavigationHandler
import us.webmy.core.ui.presentation.base.navigator.WebmyNavigator
import us.webmy.core.ui.single.SheetController

/**
 * DI module for :core-ui. Load it via `WebMY.init(..., extraModules = listOf(coreUiModule()))`.
 */
fun coreUiModule(): Module = module {
    single { SheetController() }
    single<Navigator> {
        WebmyNavigator(
            activityProvider = get(),
            biometricsServiceFactory = get(),
            sheetController = get(),
            purchaseHandler = getOrNull<PurchaseNavigationHandler>(),
            adHandler = getOrNull<AdNavigationHandler>(),
        )
    }
}

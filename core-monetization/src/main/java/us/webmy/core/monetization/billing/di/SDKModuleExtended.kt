package us.webmy.core.monetization.billing.di

import android.app.Application
import org.koin.dsl.module
import us.webmy.core.monetization.billing.domain.interactor.PremiumInteractor
import us.webmy.core.monetization.billing.domain.interactor.RealPremiumInteractor
import us.webmy.core.monetization.billing.navigator.BillingPurchaseHandler
import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.monetization.billing.tools.billing.RealBillingManager
import us.webmy.core.ui.presentation.base.navigator.PurchaseNavigationHandler

internal fun billingModule(
    application: Application,
    oneTimeProducts: Set<String>,
    subscriptionProducts: Set<String>
) = module {
    single<BillingManager> {
        RealBillingManager(
            application = application,
            activityProvider = get(),
            oneTimeProducts = oneTimeProducts,
            subscriptionProducts = subscriptionProducts
        )
    }

    single<PremiumInteractor> { RealPremiumInteractor(get()) }
    single<PurchaseNavigationHandler> {
        BillingPurchaseHandler(get())
    }
}

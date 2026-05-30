package us.webmy.core.monetization.billing.di

import android.app.Application
import org.koin.dsl.module
import us.webmy.core.monetization.billing.domain.interactor.PremiumUseCase
import us.webmy.core.monetization.billing.domain.interactor.RealPremiumUseCase
import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.monetization.billing.tools.billing.RealBillingManager

internal fun billingModule(
    application: Application,
    oneTimeProducts: Set<String>,
    subscriptionProducts: Set<String>,
    consumableProducts: Set<String>,
    premiumProductIds: Set<String>,
) = module {
    single<BillingManager> {
        RealBillingManager(
            application = application,
            activityProvider = get(),
            oneTimeProducts = oneTimeProducts,
            subscriptionProducts = subscriptionProducts,
            consumableProducts = consumableProducts,
        )
    }

    single<PremiumUseCase> { RealPremiumUseCase(get(), premiumProductIds) }
}

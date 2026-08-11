package us.webmy.core.monetization.billing.internal

import android.app.Application
import us.webmy.core.internal.di.ServiceRegistry
import us.webmy.core.monetization.billing.PremiumUseCase
import us.webmy.core.monetization.billing.internal.RealPremiumUseCase
import us.webmy.core.monetization.billing.BillingManager
import us.webmy.core.monetization.billing.internal.RealBillingManager

internal fun registerBillingServices(
    application: Application,
    oneTimeProducts: Set<String>,
    subscriptionProducts: Set<String>,
    consumableProducts: Set<String>,
    premiumProductIds: Set<String>,
) = with(ServiceRegistry) {
    register<BillingManager> {
        RealBillingManager(
            application = application,
            activityProvider = resolve(),
            oneTimeProducts = oneTimeProducts,
            subscriptionProducts = subscriptionProducts,
            consumableProducts = consumableProducts,
        )
    }

    register<PremiumUseCase> { RealPremiumUseCase(resolve(), premiumProductIds) }
}

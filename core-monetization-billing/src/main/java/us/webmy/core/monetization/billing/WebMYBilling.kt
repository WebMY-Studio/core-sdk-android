package us.webmy.core.monetization.billing

import com.apphud.sdk.Apphud
import us.webmy.core.WebMY
import us.webmy.core.internal.di.ServiceRegistry
import us.webmy.core.monetization.billing.PremiumUseCase
import us.webmy.core.monetization.billing.internal.registerBillingServices
import us.webmy.core.monetization.billing.BillingManager

private const val BILLING_NOT_INITIALIZED =
    "WebMY: Billing is not initialized. Call WebMY.initBilling(...) first."

val WebMY.billing: BillingManager
    get() = ServiceRegistry.resolve(missingMessage = BILLING_NOT_INITIALIZED)

val WebMY.premium: PremiumUseCase
    get() = ServiceRegistry.resolve(missingMessage = BILLING_NOT_INITIALIZED)

fun WebMY.initApphud(key: String) {
    Apphud.start(application, key)
}

fun WebMY.initBilling(
    oneTimeProductIds: Set<String> = emptySet(),
    subscriptionProductIds: Set<String> = emptySet(),
    consumableProductIds: Set<String> = emptySet(),
    premiumProductIds: Set<String> = emptySet(),
) {
    registerBillingServices(
        application = application,
        oneTimeProducts = oneTimeProductIds,
        subscriptionProducts = subscriptionProductIds,
        consumableProducts = consumableProductIds,
        premiumProductIds = premiumProductIds,
    )
}

package us.webmy.core.monetization.billing

import com.apphud.sdk.Apphud
import org.koin.core.context.loadKoinModules
import us.webmy.core.WebMY
import us.webmy.core.monetization.billing.di.billingModule

fun WebMY.initApphud(key: String) {
    Apphud.start(application, key)
}

fun WebMY.initBilling(
    oneTimeProductIds: Set<String> = emptySet(),
    subscriptionProductIds: Set<String> = emptySet(),
    consumableProductIds: Set<String> = emptySet(),
    premiumProductIds: Set<String> = emptySet(),
) {
    loadKoinModules(
        billingModule(
            application = application,
            oneTimeProducts = oneTimeProductIds,
            subscriptionProducts = subscriptionProductIds,
            consumableProducts = consumableProductIds,
            premiumProductIds = premiumProductIds,
        )
    )
}

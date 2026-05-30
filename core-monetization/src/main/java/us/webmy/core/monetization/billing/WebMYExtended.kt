package us.webmy.core.monetization.billing

import com.adapty.Adapty
import com.adapty.models.AdaptyConfig
import org.koin.core.context.loadKoinModules
import us.webmy.core.WebMY
import us.webmy.core.monetization.billing.di.billingModule

fun WebMY.initAdapty(key: String) {
    Adapty.activate(
        application,
        AdaptyConfig.Builder(key).build()
    )
}

fun WebMY.initBilling(
    oneTimeProductIds: Set<String> = emptySet(),
    subscriptionProductIds: Set<String> = emptySet()
) {
    loadKoinModules(
        billingModule(
            application = application,
            oneTimeProducts = oneTimeProductIds,
            subscriptionProducts = subscriptionProductIds
        )
    )
}
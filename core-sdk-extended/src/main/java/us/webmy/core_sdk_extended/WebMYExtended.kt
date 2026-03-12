package us.webmy.core_sdk_extended

import com.adapty.Adapty
import com.adapty.models.AdaptyConfig
import org.koin.core.context.loadKoinModules
import us.webmy.core_sdk.WebMY
import us.webmy.core_sdk_extended.di.billingModule

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
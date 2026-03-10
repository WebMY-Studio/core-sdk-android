package us.webmy.core_sdk_extended

import com.adapty.Adapty
import com.adapty.models.AdaptyConfig
import com.facebook.appevents.AppEventsLogger
import us.webmy.core_sdk.WebMY
import org.koin.core.context.loadKoinModules
import us.webmy.core_sdk_extended.di.billingModule
import us.webmy.core_sdk_extended.di.metaModule

fun WebMY.initAdapty(key: String) {
    Adapty.activate(
        application,
        AdaptyConfig.Builder(key).build()
    )
}

fun WebMY.initBilling(
    oneTimeProductIds: List<String> = emptyList(),
    subscriptionProductIds: List<String> = emptyList()
) {
    val oneTime = oneTimeProductIds.toSet()
    val subscriptions = subscriptionProductIds.toSet()
    if (oneTime.isNotEmpty() || subscriptions.isNotEmpty()) {

        try {
            AppEventsLogger.activateApp(application)
        } catch (_: Exception) {
        }

        loadKoinModules(
            listOf(
                metaModule(application),
                billingModule(application, oneTime, subscriptions)
            )
        )
    }
}
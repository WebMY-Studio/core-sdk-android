package us.webmy.core_sdk_extended.di

import com.facebook.appevents.AppEventsLogger
import org.koin.core.module.Module
import org.koin.dsl.module
import us.webmy.core_sdk_extended.ConfigExtended
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor
import us.webmy.core_sdk_extended.domain.interactor.RealPremiumInteractor
import us.webmy.core_sdk_extended.tools.billing.BillingManager
import us.webmy.core_sdk_extended.tools.billing.RealBillingManager

internal fun sdkModuleExtended(config: ConfigExtended) = module {
    configureAnalytics(config)
    configureBilling(config)

}

internal fun Module.configureAnalytics(config: ConfigExtended) {

    single<AppEventsLogger> { AppEventsLogger.newLogger(config.application) }
}


internal fun Module.configureBilling(config: ConfigExtended) {
    val oneTimeProducts = config.oneTimeProductIds.toSet()
    val subscriptionProducts = config.subscriptionProductIds.toSet()

    if (oneTimeProducts.isNotEmpty() || subscriptionProducts.isNotEmpty()) {
        single<BillingManager> {
            RealBillingManager(
                metaEventsLogger = get(),
                application = config.application,
                oneTimeProducts = oneTimeProducts,
                subscriptionProducts = subscriptionProducts
            )
        }

        single<PremiumInteractor> { RealPremiumInteractor(get()) }
    }
}


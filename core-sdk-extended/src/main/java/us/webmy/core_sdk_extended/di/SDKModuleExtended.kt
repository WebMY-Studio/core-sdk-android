package us.webmy.core_sdk_extended.di

import android.app.Application
import com.facebook.appevents.AppEventsLogger
import org.koin.dsl.module
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor
import us.webmy.core_sdk_extended.domain.interactor.RealPremiumInteractor
import us.webmy.core_sdk_extended.tools.billing.BillingManager
import us.webmy.core_sdk_extended.tools.billing.RealBillingManager

internal fun metaModule(application: Application) = module {
    single<AppEventsLogger> { AppEventsLogger.newLogger(application) }
}

internal fun billingModule(
    application: Application,
    oneTimeProducts: Set<String>,
    subscriptionProducts: Set<String>
) = module {
    single<BillingManager> {
        RealBillingManager(
            metaEventsLogger = get(),
            application = application,
            oneTimeProducts = oneTimeProducts,
            subscriptionProducts = subscriptionProducts
        )
    }

    single<PremiumInteractor> { RealPremiumInteractor(get()) }
}

package us.webmy.core.monetization.ads.di

import android.app.Application
import org.koin.dsl.module
import us.webmy.core.monetization.ads.navigator.AdsHandler
import us.webmy.core.monetization.ads.tools.ads.AdsManager
import us.webmy.core.monetization.ads.tools.ads.AdsPremiumManager
import us.webmy.core.monetization.ads.tools.ads.AdsPremiumManagerFactory
import us.webmy.core.monetization.ads.tools.ads.RealAdsManager
import us.webmy.core.ui.presentation.base.navigator.AdNavigationHandler

internal fun adsModule(
    application: Application,
    appodealKey: String,
    premiumProductIds: List<String>
) = module {
    single<AdsManager> {
        RealAdsManager(
            key = appodealKey,
            application = application,
            activityProvider = get(),
            analyticsManager = get(),
            firebaseAnalytics = get(),
        )
    }

    if (premiumProductIds.isNotEmpty()) {
        single<AdsPremiumManager.Factory> {
            AdsPremiumManagerFactory(
                premiumProductIds = premiumProductIds,
                billingManager = get(),
                adsManager = get(),
            )
        }
    }

    single<AdNavigationHandler> { AdsHandler(get()) }
}

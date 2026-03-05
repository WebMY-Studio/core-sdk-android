package us.webmy.core_sdk_ads.di

import android.app.Application
import org.koin.dsl.module
import us.webmy.core_sdk_ads.tools.ads.AdsManager
import us.webmy.core_sdk_ads.tools.ads.AdsPremiumManager
import us.webmy.core_sdk_ads.tools.ads.AdsPremiumManagerFactory
import us.webmy.core_sdk_ads.tools.ads.RealAdsManager

internal fun adsModule(
    application: Application,
    appodealKey: String,
    premiumProductIds: List<String>
) = module {
    single<AdsManager> {
        RealAdsManager(
            analyticsManager = get(),
            application = application,
            key = appodealKey,
            firebaseAnalytics = get()
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
}

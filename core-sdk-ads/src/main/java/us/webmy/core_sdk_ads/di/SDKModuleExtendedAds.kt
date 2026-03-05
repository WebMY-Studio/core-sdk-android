package us.webmy.core_sdk_ads.di

import org.koin.core.module.Module
import org.koin.dsl.module
import us.webmy.core_sdk_ads.ConfigExtendedAds
import us.webmy.core_sdk_ads.tools.ads.AdsManager
import us.webmy.core_sdk_ads.tools.ads.AdsPremiumManager
import us.webmy.core_sdk_ads.tools.ads.AdsPremiumManagerFactory
import us.webmy.core_sdk_ads.tools.ads.RealAdsManager

internal fun sdkModuleExtendedAds(config: ConfigExtendedAds) = module {
    configureAppodeal(config)
    configureAdsPremiumFactory(config)

}

internal fun Module.configureAppodeal(config: ConfigExtendedAds) {
    val appodealKey = config.appodealKey
    if (!appodealKey.isNullOrEmpty()) {
        single<AdsManager> {
            RealAdsManager(
                analyticsManager = get(),
                application = config.application,
                key = appodealKey,
                firebaseAnalytics = get()
            )
        }
    }
}

internal fun Module.configureAdsPremiumFactory(config: ConfigExtendedAds) {
    val premiumProductIds = config.premiumProductIds
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

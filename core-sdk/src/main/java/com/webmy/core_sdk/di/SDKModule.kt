package com.webmy.core_sdk.di

import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.ServerZone
import com.webmy.core_sdk.Config
import com.webmy.core_sdk.tools.ads.AdsManager
import com.webmy.core_sdk.tools.ads.RealAdsManager
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import com.webmy.core_sdk.tools.analytics.RealAnalyticsManager
import com.webmy.core_sdk.tools.billing.BillingManager
import com.webmy.core_sdk.tools.billing.RealBillingManager
import com.webmy.core_sdk.tools.preferences.Preferences
import com.webmy.core_sdk.tools.preferences.RealPreferences
import com.webmy.core_sdk.tools.remoteconfig.RealRemoteConfigManager
import com.webmy.core_sdk.tools.remoteconfig.RemoteConfigManager
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun sdkModule(config: Config) = module {
    configureRemoteConfig(config)
    configurePreferences(config)
    configureAmplitude(config)
    configureAppodeal(config)
    configureBilling(config)
}

internal fun Module.configureAmplitude(config: Config) {
    val amplitudeKey = config.amplitudeKey
    if (!amplitudeKey.isNullOrEmpty()) {
        single<Amplitude> {
            Amplitude(
                Configuration(
                    apiKey = amplitudeKey,
                    context = config.application,
                    serverZone = ServerZone.EU
                )
            )
        }

        single<AnalyticsManager> {
            RealAnalyticsManager(
                amplitude = get()
            )
        }
    }
}

internal fun Module.configureAppodeal(config: Config) {
    val appodealKey = config.appodealKey
    if (!appodealKey.isNullOrEmpty()) {
        single<AdsManager> {
            RealAdsManager(
                analyticsManager = get(),
                application = config.application,
                key = appodealKey
            )
        }
    }
}

internal fun Module.configureRemoteConfig(config: Config) {
    if (config.remoteConfigEnabled) {
        single<RemoteConfigManager> { RealRemoteConfigManager(config.remoteConfigUpdateInterval) }
    }
}

internal fun Module.configurePreferences(config: Config) {
    single<Preferences> { RealPreferences(config.application) }
}

internal fun Module.configureBilling(config: Config) {
    val oneTimeProducts = config.oneTimeProducts
    if (oneTimeProducts.isNotEmpty()) {
        single<BillingManager> {
            RealBillingManager(
                application = config.application,
                oneTimeProducts = oneTimeProducts
            )
        }
    }
}

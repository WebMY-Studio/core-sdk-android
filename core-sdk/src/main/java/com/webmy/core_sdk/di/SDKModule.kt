package com.webmy.core_sdk.di

import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.ServerZone
import com.webmy.core_sdk.model.Config
import com.webmy.core_sdk.tools.ads.AdsManager
import com.webmy.core_sdk.tools.ads.RealAdsManager
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import com.webmy.core_sdk.tools.analytics.RealAnalyticsManager
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun sdkModule(config: Config) = module {
    single<Config> { config }

    configureAmplitude(config)
    configureAppodeal(config)
}

internal fun Module.configureAmplitude(config: Config) {
    val amplitudeKey = config.amplitudeKey
    if (amplitudeKey != null) {
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
    if (appodealKey != null) {
        single<AdsManager> {
            RealAdsManager(
                analyticsManager = get(),
                application = config.application,
                key = appodealKey
            )
        }
    }
}
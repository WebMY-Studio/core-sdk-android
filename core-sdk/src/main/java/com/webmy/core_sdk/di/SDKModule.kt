package com.webmy.core_sdk.di

import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.ServerZone
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.webmy.core_sdk.BuildConfig
import com.webmy.core_sdk.Config
import com.webmy.core_sdk.data.NetworkApiCreator
import com.webmy.core_sdk.data.RealNetworkApiCreator
import com.webmy.core_sdk.tools.ads.AdsManager
import com.webmy.core_sdk.tools.ads.AdsPremiumManager
import com.webmy.core_sdk.tools.ads.RealAdsManager
import com.webmy.core_sdk.tools.ads.RealAdsPremiumManager
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import com.webmy.core_sdk.tools.analytics.RealAnalyticsManager
import com.webmy.core_sdk.tools.billing.BillingManager
import com.webmy.core_sdk.tools.billing.RealBillingManager
import com.webmy.core_sdk.tools.preferences.Preferences
import com.webmy.core_sdk.tools.preferences.RealPreferences
import com.webmy.core_sdk.tools.remoteconfig.RealRemoteConfigManager
import com.webmy.core_sdk.tools.remoteconfig.RemoteConfigManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.TimeUnit

internal fun sdkModule(config: Config) = module {
    configureRemoteConfig(config)
    configurePreferences(config)
    configureAnalytics(config)
    configureAppodeal(config)
    configureBilling(config)

    configureAdsPremium(config)

    configureNetwork()
}

internal fun Module.configureAnalytics(config: Config) {
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
    }
    single<FirebaseAnalytics> { FirebaseAnalytics.getInstance(config.application) }

    single<AnalyticsManager> {
        val amplitude = if (!amplitudeKey.isNullOrEmpty()) {
            get<Amplitude>()
        } else {
            null
        }

        val firebase = if (config.useFirebaseAnalytics) {
            get<FirebaseAnalytics>()
        } else {
            null
        }

        RealAnalyticsManager(
            amplitude = amplitude,
            firebase = firebase
        )
    }

}

internal fun Module.configureAppodeal(config: Config) {
    val appodealKey = config.appodealKey
    if (!appodealKey.isNullOrEmpty()) {
        single<AdsManager> {
            RealAdsManager(
                analyticsManager = get(),
                application = config.application,
                key = appodealKey,
                showDebugAds = config.showDebugAds,
                firebaseAnalytics = get()
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
    val oneTimeProducts = config.oneTimeProducts.toSet()

    val subscriptionProducts = config.subscriptionProducts.toSet()

    if (oneTimeProducts.isNotEmpty() || subscriptionProducts.isNotEmpty()) {
        single<BillingManager> {
            RealBillingManager(
                application = config.application,
                oneTimeProducts = oneTimeProducts,
                subscriptionProducts = subscriptionProducts
            )
        }
    }
}

internal fun Module.configureAdsPremium(config: Config) {
    val premiumProductId = config.premiumProductId
    if (!premiumProductId.isNullOrEmpty()) {
        single<AdsPremiumManager> {
            RealAdsPremiumManager(
                premiumProductId = premiumProductId,
                fistShowAtRemoteConfigKey = config.fistShowAtRemoteConfigKey,
                skipAdsAmountRemoteConfigKey = config.skipAdsAmountRemoteConfigKey,
                billingManager = get(),
                adsManager = get(),
                remoteConfigManager = get()
            )
        }
    }
}

private const val HTTP_CACHE = "http_cache"
private const val CACHE_SIZE = 50L * 1024L * 1024L // 50 MiB
private const val TIMEOUT_SECONDS = 20L

internal fun Module.configureNetwork() {
    single<OkHttpClient.Builder> {
        val builder =
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .cache(Cache(File(androidContext().cacheDir, HTTP_CACHE), CACHE_SIZE))
                .retryOnConnectionFailure(true)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }

        builder
    }

    single<OkHttpClient> { get<OkHttpClient.Builder>().build() }

    single<NetworkApiCreator> { RealNetworkApiCreator(get()) }

    single<Gson> { Gson() }

}

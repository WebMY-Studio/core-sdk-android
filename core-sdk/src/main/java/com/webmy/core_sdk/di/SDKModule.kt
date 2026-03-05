package com.webmy.core_sdk.di

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.ServerZone
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.webmy.core_sdk.BuildConfig
import com.webmy.core_sdk.WebMYConfig
import com.webmy.core_sdk.data.NetworkApiCreator
import com.webmy.core_sdk.data.RealNetworkApiCreator
import com.webmy.core_sdk.data.csv.CsvFetcher
import com.webmy.core_sdk.data.csv.RealCsvFetcher
import com.webmy.core_sdk.data.prefs.OnboardingShownPreferences
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import com.webmy.core_sdk.tools.analytics.RealAnalyticsManager
import com.webmy.core_sdk.tools.preferences.Preferences
import com.webmy.core_sdk.tools.preferences.RealPreferences
import com.webmy.core_sdk.tools.remoteconfig.RealRemoteConfigManager
import com.webmy.core_sdk.tools.remoteconfig.RemoteConfigManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.TimeUnit

internal fun sdkModule(config: WebMYConfig) = module {
    configureRemoteConfig(config)
    configurePreferences(config)
    configureAnalytics(config)

    configureNetwork()
    configureCsv()
}

internal fun Module.configureAnalytics(config: WebMYConfig) {
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

        RealAnalyticsManager(
            amplitude = amplitude,
            firebase = get<FirebaseAnalytics>()
        )
    }

}

internal fun Module.configureRemoteConfig(config: WebMYConfig) {
    val interval = config.remoteConfigUpdateInterval
    if (interval != null) {
        single<RemoteConfigManager> { RealRemoteConfigManager(interval.inWholeMilliseconds) }
    }
}

internal fun Module.configurePreferences(config: WebMYConfig) {
    single<Preferences> { RealPreferences(config.application) }

    single { OnboardingShownPreferences(get()) }
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

internal fun Module.configureCsv() {
    single<CsvFetcher> {
        RealCsvFetcher(get())
    }
}

inline fun <reified T> Scope.getPayload(): T {
    val activity = get<AppCompatActivity>()

    val clazz = T::class.java
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        activity.intent.getParcelableExtra(clazz.name, clazz)!!
    } else {
        activity.intent.getParcelableExtra(clazz.name)!!
    }
}

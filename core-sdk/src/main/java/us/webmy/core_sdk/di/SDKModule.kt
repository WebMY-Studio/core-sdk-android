package us.webmy.core_sdk.di

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.ServerZone
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import us.webmy.core_sdk.BuildConfig
import us.webmy.core_sdk.WebMYConfig
import us.webmy.core_sdk.data.NetworkApiCreator
import us.webmy.core_sdk.data.RealNetworkApiCreator
import us.webmy.core_sdk.data.csv.CsvFetcher
import us.webmy.core_sdk.data.csv.RealCsvFetcher
import us.webmy.core_sdk.data.prefs.OnboardingShownPreferences
import us.webmy.core_sdk.presentation.base.navigator.NavigationProvider
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import us.webmy.core_sdk.tools.analytics.RealAnalyticsManager
import us.webmy.core_sdk.tools.biometrics.data.AuthenticationSession
import us.webmy.core_sdk.tools.biometrics.data.InMemoryAuthenticationSession
import us.webmy.core_sdk.tools.biometrics.domain.BiometricsServiceFactory
import us.webmy.core_sdk.tools.biometrics.domain.RealBiometricsServiceFactory
import us.webmy.core_sdk.tools.preferences.Preferences
import us.webmy.core_sdk.tools.preferences.RealPreferences
import us.webmy.core_sdk.tools.remoteconfig.RealRemoteConfigManager
import us.webmy.core_sdk.tools.remoteconfig.RemoteConfigManager
import java.io.File
import java.util.concurrent.TimeUnit

internal fun sdkModule(config: WebMYConfig) = module {
    configureBiometrics()
    configureRemoteConfig(config)
    configurePreferences(config)
    configureAnalytics(config)

    configureNetwork()
    configureCsv()

    single { NavigationProvider() }
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

internal fun Module.configureBiometrics() {
    single<AuthenticationSession> { InMemoryAuthenticationSession() }
    factory<BiometricsServiceFactory> { RealBiometricsServiceFactory(get()) }

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

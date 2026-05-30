package us.webmy.core.di

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
import org.koin.dsl.module
import us.webmy.core.BuildConfig
import us.webmy.core.NetworkConfig
import us.webmy.core.WebMYConfig
import us.webmy.core.data.NetworkApiCreator
import us.webmy.core.data.RealNetworkApiCreator
import us.webmy.core.data.csv.CsvFetcher
import us.webmy.core.data.csv.RealCsvFetcher
import us.webmy.core.data.prefs.OnboardingShownPreferences
import us.webmy.core.tools.analytics.AnalyticsManager
import us.webmy.core.tools.analytics.RealAnalyticsManager
import us.webmy.core.tools.biometrics.data.AuthenticationSession
import us.webmy.core.tools.biometrics.data.InMemoryAuthenticationSession
import us.webmy.core.tools.biometrics.domain.BiometricsServiceFactory
import us.webmy.core.tools.biometrics.domain.RealBiometricsServiceFactory
import us.webmy.core.tools.preferences.Preferences
import us.webmy.core.tools.preferences.RealPreferences
import us.webmy.core.tools.remoteconfig.RealRemoteConfigManager
import us.webmy.core.tools.remoteconfig.RemoteConfigManager
import us.webmy.core.tools.sharing.RealSharingManager
import us.webmy.core.tools.sharing.SharingManager
import us.webmy.core.util.ActivityProvider
import us.webmy.core.util.RealActivityProvider
import java.io.File
import java.util.concurrent.TimeUnit

internal fun sdkModule(config: WebMYConfig) = module {
    configureActivityProvider(config)
    configureBiometrics()
    configureRemoteConfig(config)
    configurePreferences(config)
    configureAnalytics(config)
    configureSharing()

    configureNetwork(config.network)
    configureCsv()
}

internal fun Module.configureActivityProvider(config: WebMYConfig) {
    single<ActivityProvider>(createdAtStart = true) {
        RealActivityProvider(config.application)
    }
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

internal fun Module.configureSharing() {
    single<SharingManager> { RealSharingManager(get<ActivityProvider>()) }
}

private const val HTTP_CACHE = "http_cache"

internal fun Module.configureNetwork(network: NetworkConfig) {
    single<OkHttpClient.Builder> {
        val builder =
            OkHttpClient.Builder()
                .connectTimeout(network.connectTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                .writeTimeout(network.writeTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                .readTimeout(network.readTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                .cache(Cache(File(androidContext().cacheDir, HTTP_CACHE), network.cacheSizeBytes))
                .retryOnConnectionFailure(true)

        network.interceptors.forEach { builder.addInterceptor(it) }

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
    factory<BiometricsServiceFactory> { RealBiometricsServiceFactory(get(), get()) }
}

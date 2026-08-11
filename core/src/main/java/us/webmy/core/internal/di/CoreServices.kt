package us.webmy.core.internal.di

import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.ServerZone
import com.google.firebase.analytics.FirebaseAnalytics
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import us.webmy.core.WebMYConfig
import us.webmy.core.network.NetworkApiCreator
import us.webmy.core.internal.network.RealNetworkApiCreator
import us.webmy.core.analytics.AnalyticsManager
import us.webmy.core.internal.analytics.RealAnalyticsManager
import us.webmy.core.internal.biometrics.AuthenticationSession
import us.webmy.core.internal.biometrics.InMemoryAuthenticationSession
import us.webmy.core.biometrics.BiometricsService
import us.webmy.core.internal.biometrics.RealBiometricsService
import us.webmy.core.prefs.Preferences
import us.webmy.core.internal.prefs.RealPreferences
import us.webmy.core.internal.remoteconfig.RealRemoteConfigManager
import us.webmy.core.remoteconfig.RemoteConfigManager
import us.webmy.core.internal.sharing.RealSharingManager
import us.webmy.core.sharing.SharingManager
import us.webmy.core.util.ActivityProvider
import us.webmy.core.internal.util.RealActivityProvider
import java.io.File
import java.util.concurrent.TimeUnit

private const val HTTP_CACHE = "http_cache"

internal fun registerCoreServices(config: WebMYConfig) = with(ServiceRegistry) {
    register<ActivityProvider>(eager = true) { RealActivityProvider(config.application) }

    register<AuthenticationSession> { InMemoryAuthenticationSession() }
    register<BiometricsService> { RealBiometricsService(resolve(), resolve()) }

    val remoteConfigInterval = config.remoteConfigUpdateInterval
    if (remoteConfigInterval != null) {
        register<RemoteConfigManager> {
            RealRemoteConfigManager(remoteConfigInterval.inWholeMilliseconds)
        }
    }

    register<Preferences> { RealPreferences(config.application) }

    register<FirebaseAnalytics> { FirebaseAnalytics.getInstance(config.application) }
    register<AnalyticsManager> {
        val amplitudeKey = config.amplitudeKey
        val amplitude = if (!amplitudeKey.isNullOrEmpty()) {
            Amplitude(
                Configuration(
                    apiKey = amplitudeKey,
                    context = config.application,
                    serverZone = ServerZone.EU
                )
            )
        } else {
            null
        }

        RealAnalyticsManager(
            amplitude = amplitude,
            firebase = resolve<FirebaseAnalytics>()
        )
    }

    register<SharingManager> { RealSharingManager(resolve<ActivityProvider>()) }

    register<OkHttpClient> {
        val network = config.network
        val builder = OkHttpClient.Builder()
            .connectTimeout(network.connectTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .writeTimeout(network.writeTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .readTimeout(network.readTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .cache(Cache(File(config.application.cacheDir, HTTP_CACHE), network.cacheSizeBytes))
            .retryOnConnectionFailure(true)

        network.interceptors.forEach { builder.addInterceptor(it) }

        if (network.enableHttpLogging) {
            builder.addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }

        builder.build()
    }

    register<NetworkApiCreator> { RealNetworkApiCreator(resolve()) }
}

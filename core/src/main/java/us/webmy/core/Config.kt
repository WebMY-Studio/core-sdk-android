package us.webmy.core

import android.app.Application
import okhttp3.Interceptor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class WebMYConfig(
    val application: Application,
    val koinMode: KoinMode = KoinMode.START,
    val amplitudeKey: String? = null,
    val remoteConfigUpdateInterval: Duration? = null,
    val network: NetworkConfig = NetworkConfig(),
)

class NetworkConfig(
    val connectTimeout: Duration = 20.seconds,
    val readTimeout: Duration = 20.seconds,
    val writeTimeout: Duration = 20.seconds,
    val cacheSizeBytes: Long = 50L * 1024L * 1024L,
    val interceptors: List<Interceptor> = emptyList(),
)

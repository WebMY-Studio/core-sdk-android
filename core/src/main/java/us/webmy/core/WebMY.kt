package us.webmy.core

import android.app.Application
import android.util.Log
import okhttp3.OkHttpClient
import us.webmy.core.network.NetworkApiCreator
import us.webmy.core.internal.di.ServiceRegistry
import us.webmy.core.internal.di.registerCoreServices
import us.webmy.core.analytics.AnalyticsManager
import us.webmy.core.biometrics.BiometricsService
import us.webmy.core.prefs.Preferences
import us.webmy.core.remoteconfig.RemoteConfigManager
import us.webmy.core.sharing.SharingManager
import us.webmy.core.prefs.OnboardingShownPreferences
import us.webmy.core.theme.WebmyThemeController
import us.webmy.core.navigation.Router
import us.webmy.core.util.ActivityProvider

object WebMY {

    @Volatile
    private var _application: Application? = null

    val application: Application
        get() = _application ?: error("WebMY.init(...) not called")

    fun init(config: WebMYConfig) {
        if (_application != null) {
            Log.w("WebMY", "init() called more than once — ignoring")
            return
        }
        _application = config.application
        registerCoreServices(config)
    }

    val analytics: AnalyticsManager get() = ServiceRegistry.resolve()

    val preferences: Preferences get() = ServiceRegistry.resolve()

    val sharing: SharingManager get() = ServiceRegistry.resolve()

    val biometrics: BiometricsService get() = ServiceRegistry.resolve()

    val network: NetworkApiCreator get() = ServiceRegistry.resolve()

    val httpClient: OkHttpClient get() = ServiceRegistry.resolve()

    val activityProvider: ActivityProvider get() = ServiceRegistry.resolve()

    val remoteConfig: RemoteConfigManager
        get() = ServiceRegistry.resolve(
            missingMessage = "WebMY: RemoteConfig is not enabled. " +
                "Pass remoteConfigUpdateInterval to WebMYConfig."
        )

    val theme: WebmyThemeController get() = ServiceRegistry.resolve(missingMessage = UI_NOT_INSTALLED)

    val router: Router get() = ServiceRegistry.resolve(missingMessage = UI_NOT_INSTALLED)

    val onboardingPreferences: OnboardingShownPreferences
        get() = ServiceRegistry.resolve(missingMessage = UI_NOT_INSTALLED)
}

private const val UI_NOT_INSTALLED =
    "WebMY: UI services not installed. Call WebMY.installUi() after WebMY.init()."

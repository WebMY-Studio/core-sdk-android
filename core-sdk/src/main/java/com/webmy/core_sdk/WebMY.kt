package com.webmy.core_sdk

import com.google.android.gms.ads.MobileAds
import com.webmy.core_sdk.di.sdkModule
import com.webmy.core_sdk.model.Config
import com.webmy.core_sdk.model.KoinMode
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

/**
 * Firebase integration guide:
 *
 * To enable Firebase services in your app, follow these steps:
 *
 * 1. Add the `google-services.json` file to your **app module**.
 *
 * 2. In your app module’s `build.gradle.kts`, apply the plugins:
 *    ```
 *    alias(libs.plugins.google.services)
 *    alias(libs.plugins.firebase.crashlytics)
 *    ```
 *
 * 3. In your **root** `build.gradle.kts`, declare the same plugins with `apply false`:
 *    ```
 *    alias(libs.plugins.google.services) apply false
 *    alias(libs.plugins.firebase.crashlytics) apply false
 *    ```
 */
class WebMY private constructor() {

    companion object {
        val INSTANCE = WebMY()
    }

    fun init(config: Config) {
        initKoin(config)

        MobileAds.initialize(config.application)
    }

    private fun initKoin(config: Config) {
        val module = sdkModule(config)
        when (config.koinMode) {
            KoinMode.START -> startKoin { modules(module) }
            KoinMode.LOAD -> loadKoinModules(module)
        }
    }
}
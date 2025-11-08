package com.webmy.coresdkdemo

import android.app.Application
import com.webmy.core_sdk.WebMY
import com.webmy.core_sdk.model.Config
import com.webmy.core_sdk.model.KoinMode

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = Config.Builder(this)
            .setKoinMode(KoinMode.START)
            .useFirebaseRemoteConfig()
            .addAppodealKey(BuildConfig.APPODEAL_APP_KEY)
            .addAmplitudeKey(BuildConfig.AMPLITUDE_API_KEY)
            .build()

        WebMY.INSTANCE.init(config)
    }
}
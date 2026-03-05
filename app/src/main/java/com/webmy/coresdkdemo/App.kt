package com.webmy.coresdkdemo

import android.app.Application
import com.webmy.core_sdk.Config
import com.webmy.core_sdk.KoinMode
import com.webmy.core_sdk.WebMY

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = Config.Builder(this)
            .setKoinMode(KoinMode.START)
            .build()

        WebMY<Config>().init(config)
    }
}
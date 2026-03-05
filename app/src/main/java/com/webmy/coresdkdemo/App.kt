package com.webmy.coresdkdemo

import android.app.Application
import com.webmy.core_sdk.KoinMode
import com.webmy.core_sdk.WebMY
import com.webmy.core_sdk.WebMYConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(
            WebMYConfig(
                application = this,
                koinMode = KoinMode.START,
            )
        )
    }
}

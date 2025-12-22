package com.webmy.core_sdk

import com.adapty.Adapty
import com.adapty.models.AdaptyConfig
import com.google.android.gms.ads.MobileAds
import com.webmy.core_sdk.di.sdkModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin


class WebMY private constructor() {

    companion object {
        val INSTANCE = WebMY()
    }

    fun init(config: Config) {
        initKoin(config)

        if (!config.appodealKey.isNullOrEmpty()) {
            MobileAds.initialize(config.application)
        }

        val adaptyKey = config.adaptyKey
        if (!adaptyKey.isNullOrEmpty()) {
            Adapty.activate(
                config.application,
                AdaptyConfig.Builder(adaptyKey).build()
            )
        }
    }

    private fun initKoin(config: Config) {
        val module = sdkModule(config)
        when (config.koinMode) {
            KoinMode.START -> startKoin { modules(module) }
            KoinMode.LOAD -> loadKoinModules(module)
        }
    }
}
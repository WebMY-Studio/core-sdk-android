package com.webmy.core_sdk

import com.webmy.core_sdk.di.sdkModule
import com.webmy.core_sdk.model.Config
import com.webmy.core_sdk.model.KoinMode
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

class WebMY private constructor() {

    companion object {
        val INSTANCE = WebMY()
    }

    fun init(config: Config) {
        initKoin(config)
    }

    private fun initKoin(config: Config) {
        val module = sdkModule(config)
        when (config.koinMode) {
            KoinMode.START -> startKoin { modules(module) }
            KoinMode.LOAD -> loadKoinModules(module)
        }
    }
}
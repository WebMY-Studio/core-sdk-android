package com.webmy.core_sdk

import androidx.annotation.CallSuper
import com.webmy.core_sdk.di.sdkModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

open class WebMY<T : Config> {

    @CallSuper
    open fun init(config: T) {
        initKoin(config)
    }

    private fun initKoin(config: T) {
        val module = sdkModule(config)
        when (config.koinMode) {
            KoinMode.START -> startKoin { modules(module) }
            KoinMode.LOAD -> loadKoinModules(module)
        }
    }
}
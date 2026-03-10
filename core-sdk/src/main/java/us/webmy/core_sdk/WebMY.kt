package us.webmy.core_sdk

import android.app.Application
import us.webmy.core_sdk.di.sdkModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

object WebMY {

    lateinit var application: Application
        private set

    fun init(config: WebMYConfig) {
        application = config.application
        val module = sdkModule(config)
        when (config.koinMode) {
            KoinMode.START -> startKoin { modules(module) }
            KoinMode.LOAD -> loadKoinModules(module)
        }
    }
}

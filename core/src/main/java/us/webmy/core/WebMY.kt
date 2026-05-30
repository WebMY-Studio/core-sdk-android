package us.webmy.core

import android.app.Application
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import us.webmy.core.di.sdkModule

object WebMY {

    lateinit var application: Application
        private set

    fun init(config: WebMYConfig, extraModules: List<Module> = emptyList()) {
        application = config.application
        val modules = listOf(sdkModule(config)) + extraModules
        when (config.koinMode) {
            KoinMode.START -> startKoin { modules(modules) }
            KoinMode.LOAD -> loadKoinModules(modules)
        }
    }
}

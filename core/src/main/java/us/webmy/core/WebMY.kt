package us.webmy.core

import android.app.Application
import android.util.Log
import androidx.annotation.VisibleForTesting
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import us.webmy.core.di.sdkModule

object WebMY {

    @Volatile
    private var _application: Application? = null

    val application: Application
        get() = _application ?: error("WebMY.init(...) not called")

    fun init(config: WebMYConfig, extraModules: List<Module> = emptyList()) {
        if (_application != null) {
            Log.w("WebMY", "init() called more than once — ignoring")
            return
        }
        _application = config.application
        val modules = listOf(sdkModule(config)) + extraModules
        when (config.koinMode) {
            KoinMode.START -> startKoin {
                androidContext(config.application)
                modules(modules)
            }
            KoinMode.LOAD -> loadKoinModules(modules)
        }
    }
}

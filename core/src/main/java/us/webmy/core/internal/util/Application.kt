package us.webmy.core.internal.util

import us.webmy.core.internal.InternalWebmyApi
import android.app.Application

@InternalWebmyApi
fun Application.isHostDebuggable(): Boolean {
    return (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

@InternalWebmyApi
fun Application.isHostInDebugMode(): Boolean {
    return runCatching {
        val clazz = Class.forName("${packageName}.BuildConfig")
        val field = clazz.getField("DEBUG")
        field.getBoolean(null)
    }.getOrElse {
        isHostDebuggable()
    }
}
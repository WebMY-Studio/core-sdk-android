package us.webmy.core.util

import android.app.Application

fun Application.isHostDebuggable(): Boolean {
    return (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

fun Application.isHostInDebugMode(): Boolean {
    return runCatching {
        val clazz = Class.forName("${packageName}.BuildConfig")
        val field = clazz.getField("DEBUG")
        field.getBoolean(null)
    }.getOrElse {
        isHostDebuggable()
    }
}
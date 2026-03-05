package com.webmy.core_sdk

import android.app.Application
import kotlin.time.Duration

class WebMYConfig(
    val application: Application,
    val koinMode: KoinMode = KoinMode.START,
    val amplitudeKey: String? = null,
    val remoteConfigUpdateInterval: Duration? = null,
)

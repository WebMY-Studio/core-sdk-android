package com.webmy.core_sdk.presentation.base.viewmodel

import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel

open class BaseViewModel() : ViewModel() {
    protected inline fun <reified T> Intent.getPayload(): T {
        val clazz = T::class.java
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(clazz.name, clazz)!!
        } else {
            getParcelableExtra(clazz.name)!!
        }
    }
}
package com.webmy.core_sdk.util

fun <T> Result<T>.coerceToUnit(): Result<Unit> = map { }
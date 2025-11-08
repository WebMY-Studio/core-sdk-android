package com.webmy.core_sdk.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend inline fun Flow<Boolean>.awaitTrue() {
    first { it }
}
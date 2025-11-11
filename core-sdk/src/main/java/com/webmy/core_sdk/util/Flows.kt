package com.webmy.core_sdk.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

fun <T> singleReplaySharedFlow() =
    MutableSharedFlow<T>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

suspend inline fun Flow<Boolean>.awaitTrue() {
    first { it }
}

fun <V> Flow<V>.observe(owner: LifecycleOwner, collector: suspend (V) -> Unit) {
    owner.lifecycleScope.launchWhenResumed {
        collect(collector)
    }
}

inline fun <T, R> Flow<List<T>>.mapList(crossinline mapper: suspend (T) -> R) =
    map { list ->
        list.map { item -> mapper(item) }
    }

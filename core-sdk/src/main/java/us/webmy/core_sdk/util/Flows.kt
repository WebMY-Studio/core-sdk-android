package us.webmy.core_sdk.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun <T> singleReplaySharedFlow() =
    MutableSharedFlow<T>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

suspend inline fun Flow<Boolean>.awaitTrue() {
    first { it }
}

fun <V> Flow<V>.observe(
    owner: LifecycleOwner,
    collector: suspend (V) -> Unit,
    state: Lifecycle.State = Lifecycle.State.RESUMED
) {
    observe(owner, state, collector)
}

fun <V> Flow<V>.observe(
    owner: LifecycleOwner,
    state: Lifecycle.State,
    collector: suspend (V) -> Unit
) {
    owner.lifecycleScope.launch {
        owner.lifecycle.repeatOnLifecycle(state) {
            collect(collector)
        }
    }
}

inline fun <T, R> Flow<List<T>>.mapList(crossinline mapper: suspend (T) -> R) =
    map { list ->
        list.map { item -> mapper(item) }
    }

inline fun <T, R> Flow<List<T>>.mapListNotNull(crossinline mapper: suspend (T) -> R?) =
    map { list ->
        list.mapNotNull { item -> mapper(item) }
    }


fun currentTimestampFlow(interval: Duration): Flow<Long> {
    return flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(interval)
        }
    }
}

fun <T> flowOf(producer: suspend () -> T) = flow {
    emit(producer())
}

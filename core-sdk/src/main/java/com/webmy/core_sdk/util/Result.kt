package com.webmy.core_sdk.util

fun <T> Result<T>.coerceToUnit(): Result<Unit> = map { }

inline fun <T, R> Result<List<T>>.mapList(crossinline mapper: (T) -> R) =
    map { list ->
        list.map { item -> mapper(item) }
    }

@Suppress("UNCHECKED_CAST")
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return fold(
        onSuccess = { transform(it) },
        onFailure = { this as Result<R> }
    )
}

fun <T> failure(message: String) = Result.failure<T>(Throwable(message))
package com.webmy.core_sdk.data.csv

interface CsvMapper<T> {

    suspend fun map(from: String): Result<T>
}
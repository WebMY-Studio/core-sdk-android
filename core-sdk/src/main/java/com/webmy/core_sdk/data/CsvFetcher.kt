package com.webmy.core_sdk.data

import com.webmy.core_sdk.data.csv.CsvMapper
import com.webmy.core_sdk.util.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

interface CsvFetcher {
    companion object {
        const val CSV_ROW_DIVIDER = "\r\n"

        const val CSV_COLUMN_DIVIDER = ","
    }

    suspend fun byUrl(url: String): Result<String>

    suspend fun <T> byUrl(url: String, mapper: CsvMapper<T>): Result<T>
}

internal class RealCsvFetcher(
    private val okHttpClient: OkHttpClient
) : CsvFetcher {

    override suspend fun byUrl(url: String): Result<String> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body.string()
                } else {
                    throw IOException("HTTP error code ${response.code}")
                }
            }
        }
    }

    override suspend fun <T> byUrl(url: String, mapper: CsvMapper<T>): Result<T> {
        return byUrl(url)
            .flatMap {
                mapper.map(it)
            }
    }
}

fun String.removeQuotes() = replace("\"", "")

fun String.rollbackCommas(replaceWith: String = "|") = replace(replaceWith, ",")
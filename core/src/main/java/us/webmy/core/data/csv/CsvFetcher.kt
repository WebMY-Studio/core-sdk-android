package us.webmy.core.data.csv

import us.webmy.core.error.SdkError
import us.webmy.core.util.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

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
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder().url(url).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw SdkError.Network.HttpError(response.code, response.message)
                    }
                    response.body?.string() ?: throw SdkError.Network.EmptyBody()
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
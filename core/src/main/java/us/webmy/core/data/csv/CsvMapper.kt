package us.webmy.core.data.csv

interface CsvMapper<T> {

    suspend fun map(from: String): Result<T>
}
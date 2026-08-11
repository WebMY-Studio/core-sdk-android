package us.webmy.core.remoteconfig


interface RemoteConfigManager {

    suspend fun getString(key: String): Result<String>

    suspend fun getBoolean(key: String): Result<Boolean>

    suspend fun getLong(key: String): Result<Long>

    suspend fun getDouble(key: String): Result<Double>
}

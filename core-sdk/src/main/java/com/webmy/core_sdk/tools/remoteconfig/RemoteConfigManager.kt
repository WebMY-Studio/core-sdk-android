package com.webmy.core_sdk.tools.remoteconfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.webmy.core_sdk.util.awaitTrue
import com.webmy.core_sdk.util.executeSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface RemoteConfigManager {

    suspend fun getSyncedString(key: String): Result<String>

    suspend fun getString(key: String): Result<String>

    suspend fun getSyncedBoolean(key: String): Result<Boolean>

    suspend fun getBoolean(key: String): Result<Boolean>

    suspend fun getSyncedLong(key: String): Result<Long>

    suspend fun getLong(key: String): Result<Long>
}

internal class RealRemoteConfigManager(updateInterval: Long) : RemoteConfigManager {

    private val isSynced = MutableStateFlow(false)
    private val syncingMutex = Mutex()

    private val remoteConfig = Firebase.remoteConfig

    init {
        CoroutineScope(Dispatchers.IO)
            .launch {
                remoteConfig.setConfigSettingsAsync(
                    remoteConfigSettings { minimumFetchIntervalInSeconds = updateInterval }
                )

                syncingMutex.withLock {
                    isSynced.value = false

                    remoteConfig.fetchAndActivate()
                        .executeSuspend()
                        .onSuccess {
                            isSynced.value = true
                        }
                }
            }
    }

    override suspend fun getSyncedString(key: String): Result<String> {
        return getSyncedValue { remoteConfig.getString(key) }
    }

    override suspend fun getString(key: String): Result<String> {
        return runCatching { remoteConfig.getString(key) }
    }

    override suspend fun getSyncedBoolean(key: String): Result<Boolean> {
        return getSyncedValue { remoteConfig.getBoolean(key) }
    }

    override suspend fun getBoolean(key: String): Result<Boolean> {
        return runCatching { remoteConfig.getBoolean(key) }
    }

    override suspend fun getSyncedLong(key: String): Result<Long> {
        return getSyncedValue { remoteConfig.getLong(key) }
    }

    override suspend fun getLong(key: String): Result<Long> {
        return runCatching { remoteConfig.getLong(key) }
    }

    private suspend inline fun <R> getSyncedValue(retrieve: () -> R): Result<R> {
        return runCatching {
            isSynced.awaitTrue()
            retrieve()
        }
    }
}
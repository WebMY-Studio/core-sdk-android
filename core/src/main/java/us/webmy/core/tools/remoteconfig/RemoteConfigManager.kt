package us.webmy.core.tools.remoteconfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import us.webmy.core.util.awaitTrue
import us.webmy.core.util.executeSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface RemoteConfigManager {

    suspend fun getString(key: String): Result<String>

    suspend fun getBoolean(key: String): Result<Boolean>

    suspend fun getLong(key: String): Result<Long>

    suspend fun getDouble(key: String): Result<Double>
}

internal class RealRemoteConfigManager(updateInterval: Long) : RemoteConfigManager {

    private val isSynced = MutableStateFlow(false)
    private val syncingMutex = Mutex()

    private val remoteConfig = Firebase.remoteConfig

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
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

    override suspend fun getString(key: String) = getSyncedValue { remoteConfig.getString(key) }

    override suspend fun getBoolean(key: String) = getSyncedValue { remoteConfig.getBoolean(key) }

    override suspend fun getLong(key: String) = getSyncedValue { remoteConfig.getLong(key) }

    override suspend fun getDouble(key: String) = getSyncedValue { remoteConfig.getDouble(key) }

    private suspend inline fun <R> getSyncedValue(retrieve: () -> R): Result<R> {
        return runCatching {
            isSynced.awaitTrue()
            retrieve()
        }
    }
}
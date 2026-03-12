package us.webmy.core_sdk.tools.biometrics.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import us.webmy.core_sdk.tools.biometrics.data.AuthenticationMethod
import us.webmy.core_sdk.tools.biometrics.data.AuthenticationSession

interface BiometricsService {
    /**
     * Performs a once-per-session authentication
     * The function will immediately return if user has already authenticated in the current app session
     */
    suspend fun performSessionAuthentication(): Result<Unit>

    suspend fun performOneTimeAuthentication(): Result<Unit>
}


internal class RealBiometricsService(
    private val authMethod: AuthenticationMethod,
    private val authenticationSession: AuthenticationSession,
) : BiometricsService {
    private val authMutex = Mutex()

    override suspend fun performSessionAuthentication() = authMutex.withLock {
        if (authenticationSession.isAuthenticated()) return Result.success(Unit)

        authMethod.authenticateUser().onSuccess {
            authenticationSession.markAuthenticated()
        }
    }

    override suspend fun performOneTimeAuthentication() = authMutex.withLock {
        authMethod.authenticateUser()
    }
}
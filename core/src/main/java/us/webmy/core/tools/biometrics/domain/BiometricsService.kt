package us.webmy.core.tools.biometrics.domain

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import us.webmy.core.error.SdkError
import us.webmy.core.tools.biometrics.data.AuthenticationMethod
import us.webmy.core.tools.biometrics.data.AuthenticationSession
import us.webmy.core.tools.biometrics.data.methods.BiometricPromptAuthenticationMethod
import us.webmy.core.util.ActivityProvider

interface BiometricsService {
    /**
     * Performs a once-per-session authentication
     * The function will immediately return if user has already authenticated in the current app session
     */
    suspend fun performSessionAuthentication(): Result<Unit>

    suspend fun performOneTimeAuthentication(): Result<Unit>
}

internal class RealBiometricsService(
    private val activityProvider: ActivityProvider,
    private val authenticationSession: AuthenticationSession,
) : BiometricsService {

    private val authMutex = Mutex()

    override suspend fun performSessionAuthentication() = authMutex.withLock {
        if (authenticationSession.isAuthenticated()) return Result.success(Unit)

        currentMethod().authenticateUser().onSuccess {
            authenticationSession.markAuthenticated()
        }
    }

    override suspend fun performOneTimeAuthentication() = authMutex.withLock {
        currentMethod().authenticateUser()
    }

    private fun currentMethod(): AuthenticationMethod {
        val activity = activityProvider.requireCurrent() as? FragmentActivity
            ?: throw SdkError.NotSupported("BiometricPrompt requires a FragmentActivity")
        return BiometricPromptAuthenticationMethod(activity)
    }
}

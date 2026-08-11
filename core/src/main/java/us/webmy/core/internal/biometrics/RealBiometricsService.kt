package us.webmy.core.internal.biometrics

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import us.webmy.core.SdkError
import us.webmy.core.biometrics.BiometricsService
import us.webmy.core.util.ActivityProvider

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

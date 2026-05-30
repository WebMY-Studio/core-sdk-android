package us.webmy.core.tools.biometrics.domain

import androidx.fragment.app.FragmentActivity
import us.webmy.core.error.SdkError
import us.webmy.core.tools.biometrics.data.AuthenticationSession
import us.webmy.core.tools.biometrics.data.methods.BiometricPromptAuthenticationMethod
import us.webmy.core.util.ActivityProvider

interface BiometricsServiceFactory {
    fun create(): BiometricsService
}

internal class RealBiometricsServiceFactory(
    private val activityProvider: ActivityProvider,
    private val authenticationSession: AuthenticationSession,
) : BiometricsServiceFactory {

    override fun create(): BiometricsService {
        val activity = activityProvider.requireCurrent() as? FragmentActivity
            ?: throw SdkError.NotSupported("BiometricPrompt requires a FragmentActivity")
        val authMethod = BiometricPromptAuthenticationMethod(activity)
        return RealBiometricsService(authMethod, authenticationSession)
    }
}

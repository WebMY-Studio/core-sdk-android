package us.webmy.core_sdk.tools.biometrics.domain

import androidx.appcompat.app.AppCompatActivity
import us.webmy.core_sdk.tools.biometrics.data.AuthenticationSession
import us.webmy.core_sdk.tools.biometrics.data.methods.BiometricPromptAuthenticationMethod

/**
 * Factory for creating [BiometricsService] bound to a specific Activity.
 * Injected in Activity; call [create] with the Activity instance to obtain the service.
 */
interface BiometricsServiceFactory {
    fun create(activity: AppCompatActivity): BiometricsService
}

internal class RealBiometricsServiceFactory(
    private val authenticationSession: AuthenticationSession,
) : BiometricsServiceFactory {

    override fun create(activity: AppCompatActivity): BiometricsService {
        val authMethod = BiometricPromptAuthenticationMethod(activity)
        return RealBiometricsService(authMethod, authenticationSession)
    }
}

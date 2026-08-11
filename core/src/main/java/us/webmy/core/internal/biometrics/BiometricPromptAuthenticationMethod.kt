package us.webmy.core.internal.biometrics

import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import us.webmy.core.R
import us.webmy.core.internal.biometrics.AuthenticationMethod
import us.webmy.core.biometrics.AuthenticationCancelledException
import us.webmy.core.util.flatMap
import kotlin.coroutines.resume

internal class BiometricPromptAuthenticationMethod(
    private val activity: FragmentActivity
) :
    AuthenticationMethod {

    companion object {
        private const val ALLOWED_BIOMETRICS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
    }

    private val executor = ContextCompat.getMainExecutor(activity)

    override suspend fun authenticateUser(): Result<Unit> {
        return authenticateUseInternal()
    }

    private suspend fun authenticateUseInternal(): Result<Unit> {
        val biometricManager = BiometricManager.from(activity)
        val context = activity.applicationContext

        return prepareForAuthentication(biometricManager)
            .flatMap {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(context.getString(R.string.authentication_title))
                    .setDescription(context.getString(R.string.authentication_description))
                    .setAllowedAuthenticators(ALLOWED_BIOMETRICS)
                    .build()

                promptInfo.authenticate()
            }
    }

    private suspend fun BiometricPrompt.PromptInfo.authenticate(): Result<Unit> {
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val biometricPrompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence,
                        ) {
                            val exception =
                                AuthenticationCancelledException("$errorCode: $errString")
                            continuation.resume(Result.failure(exception))
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                )

                continuation.invokeOnCancellation {
                    biometricPrompt.cancelAuthentication()
                }

                biometricPrompt.authenticate(this@authenticate)
            }
        }
    }

    private suspend fun prepareForAuthentication(biometricManager: BiometricManager): Result<Unit> {
        return when (biometricManager.canAuthenticate(ALLOWED_BIOMETRICS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Result.success(Unit)

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Result.failure(AuthenticationCancelledException("No matching hardware found to perform authentication"))

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Result.failure(AuthenticationCancelledException("Hardware to perform authentication is currently unavailable. Try again later."))

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricExecutor(activity).execute()

            else -> Result.failure(AuthenticationCancelledException("Unknown authentication hardware state"))
        }
    }
}

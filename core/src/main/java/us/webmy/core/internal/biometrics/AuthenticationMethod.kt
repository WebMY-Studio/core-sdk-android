package us.webmy.core.internal.biometrics

internal interface AuthenticationMethod {
    suspend fun authenticateUser(): Result<Unit>
}

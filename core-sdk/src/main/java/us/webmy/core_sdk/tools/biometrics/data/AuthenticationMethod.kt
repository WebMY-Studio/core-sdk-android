package us.webmy.core_sdk.tools.biometrics.data

interface AuthenticationMethod {
    suspend fun authenticateUser(): Result<Unit>
}

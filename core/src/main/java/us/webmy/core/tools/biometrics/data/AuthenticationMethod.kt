package us.webmy.core.tools.biometrics.data

interface AuthenticationMethod {
    suspend fun authenticateUser(): Result<Unit>
}

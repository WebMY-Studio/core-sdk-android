package us.webmy.core.internal.biometrics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

internal interface AuthenticationSession {
    val hasAuthenticatedInCurrentSession: Flow<Boolean>

    fun isAuthenticated(): Boolean

    fun markAuthenticated()
}

internal suspend fun AuthenticationSession.awaitAuthenticated() =
    hasAuthenticatedInCurrentSession.filter { it }.first()

internal class InMemoryAuthenticationSession() : AuthenticationSession {
    override val hasAuthenticatedInCurrentSession = MutableStateFlow(false)

    override fun isAuthenticated(): Boolean {
        return hasAuthenticatedInCurrentSession.value
    }

    override fun markAuthenticated() {
        hasAuthenticatedInCurrentSession.value = true
    }
}


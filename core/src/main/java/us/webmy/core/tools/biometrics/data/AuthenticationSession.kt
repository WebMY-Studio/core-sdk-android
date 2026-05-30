package us.webmy.core.tools.biometrics.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

interface AuthenticationSession {
    val hasAuthenticatedInCurrentSession: Flow<Boolean>

    fun isAuthenticated(): Boolean

    fun markAuthenticated()
}

suspend fun AuthenticationSession.awaitAuthenticated() =
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


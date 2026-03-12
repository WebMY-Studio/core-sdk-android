package us.webmy.core_sdk.tools.biometrics.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface AuthenticationSession {
    val hasAuthenticatedInCurrentSession: Flow<Boolean>

    fun isAuthenticated(): Boolean

    fun markAuthenticated()
}

internal class InMemoryAuthenticationSession() : AuthenticationSession {
    override val hasAuthenticatedInCurrentSession = MutableStateFlow(false)

    override fun isAuthenticated(): Boolean {
        return hasAuthenticatedInCurrentSession.value
    }

    override fun markAuthenticated() {
        hasAuthenticatedInCurrentSession.value = true
    }
}


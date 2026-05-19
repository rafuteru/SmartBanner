package lab.smartbanner.domain

import kotlinx.coroutines.flow.StateFlow

interface AccessCodeRepository {
    val authState: StateFlow<AuthState>
    suspend fun signInWithCode(code: String)
    suspend fun signOut()
    suspend fun isAuthenticated(): Boolean
    suspend fun getAccessCode(): String
    suspend fun hasSeenInitialDialog(): Boolean
    suspend fun setHasSeenInitialDialog(seen: Boolean)
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

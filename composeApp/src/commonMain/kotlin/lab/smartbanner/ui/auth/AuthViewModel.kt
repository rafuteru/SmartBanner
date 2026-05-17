package lab.smartbanner.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.AuthState

class AuthViewModel(
    private val authRepository: AccessCodeRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Idle)

    fun signInWithCode(code: String) {
        viewModelScope.launch {
            authRepository.signInWithCode(code)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

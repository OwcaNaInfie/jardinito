package pl.edu.pb.jardinito.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.edu.pb.jardinito.data.model.profile.User
import pl.edu.pb.jardinito.viewmodel.state.AuthState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _pendingUserId = MutableStateFlow<String?>(null)
    val pendingUserId: StateFlow<String?> = _pendingUserId

    private val _pendingEmail = MutableStateFlow<String?>(null)
    val pendingEmail: StateFlow<String?> = _pendingEmail

    fun setUiState(state: AuthState) {
        _uiState.value = state
    }

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun updateCurrentUser(update: (User?) -> User?) {
        _currentUser.value = update(_currentUser.value)
    }

    fun setPendingUserId(userId: String?) {
        _pendingUserId.value = userId
    }

    fun setPendingEmail(email: String?) {
        _pendingEmail.value = email
    }

    fun clearPendingData() {
        _pendingUserId.value = null
        _pendingEmail.value = null
    }

    fun clearSession() {
        _currentUser.value = null
        _uiState.value = AuthState.Idle
    }

    fun resetUiState() {
        if (_uiState.value !is AuthState.Loading) {
            _uiState.value = AuthState.Idle
        }
    }
}
package pl.edu.pb.jardinito.data.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.edu.pb.jardinito.data.model.profile.Avatar
import pl.edu.pb.jardinito.data.model.profile.User
import pl.edu.pb.jardinito.viewmodel.state.AuthState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _pendingUserId = MutableStateFlow<String?>(null)
    val pendingUserId: StateFlow<String?> = _pendingUserId

    private val _pendingEmail = MutableStateFlow<String?>(null)
    val pendingEmail: StateFlow<String?> = _pendingEmail

    init {
        val savedUser = restoreSession()
        if (savedUser != null) {
            _currentUser.value = savedUser
            _uiState.value = AuthState.SessionRestored
        }
    }

    // =====================
    // SESSION PERSISTENCE
    // =====================

    private fun saveSession(user: User) {
        prefs.edit()
            .putString("userId", user.userId)
            .putString("username", user.username)
            .putString("email", user.email)
            .putString("avatarDefault", user.avatar.default)
            .putString("avatarCustom", user.avatar.custom)
            .putString("avatarGoogle", user.avatar.google)
            .apply()
    }

    private fun restoreSession(): User? {
        val userId = prefs.getString("userId", null) ?: return null
        val username = prefs.getString("username", null) ?: return null
        val email = prefs.getString("email", null) ?: return null
        return User(
            userId = userId,
            username = username,
            email = email,
            avatar = Avatar(
                default = prefs.getString("avatarDefault", null) ?: "",
                custom = prefs.getString("avatarCustom", null),
                google = prefs.getString("avatarGoogle", null)
            )
        )
    }

    // =====================
    // STATE MANAGEMENT
    // =====================

    fun setUiState(state: AuthState) {
        _uiState.value = state
    }

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
        if (user != null) saveSession(user) else clearPrefs()
    }

    fun updateCurrentUser(update: (User?) -> User?) {
        val updated = update(_currentUser.value)
        _currentUser.value = updated
        if (updated != null) saveSession(updated)
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
        clearPrefs()
    }

    fun resetUiState() {
        if (_uiState.value !is AuthState.Loading) {
            _uiState.value = AuthState.Idle
        }
    }

    private fun clearPrefs() {
        prefs.edit().clear().apply()
    }
}
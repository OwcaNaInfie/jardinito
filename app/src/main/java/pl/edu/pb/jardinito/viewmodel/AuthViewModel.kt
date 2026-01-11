package pl.edu.pb.jardinito.viewmodel

import AuthRepository
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.model.User

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.login(email, password)
                Log.d("JARDINITO", "DEBUG login response: $response")
                val username = response.username
                val userEmail = response.email

                if (username == null || userEmail == null) {
                    _uiState.value = AuthState.Error("Invalid server response")
                    return@launch
                }

                _currentUser.value = User(username = username, email = userEmail)

                _uiState.value = AuthState.Success(response.message)
            } catch (e: Exception) {
                _uiState.value = AuthState.Error("Login failed")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.googleLogin(idToken)

                val email = response.email
                val username = response.username

                if (email == null || username == null) {
                    _uiState.value = AuthState.Error("Invalid Google response")
                    return@launch
                }

                _currentUser.value = User(
                    email = email,
                    username = username
                )

                _uiState.value = AuthState.Success(response.message)

            } catch (e: Exception) {
                _uiState.value = AuthState.Error("Google login failed")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.register(username, email, password)

                val usernameResp = response.username
                val userEmail = response.email

                if (usernameResp == null || userEmail == null) {
                    _uiState.value = AuthState.Error("Invalid server response")
                    return@launch
                }

                _currentUser.value = User(username = usernameResp, email = userEmail)

                _uiState.value = AuthState.Success(response.message)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _uiState.value = AuthState.Idle
    }
}

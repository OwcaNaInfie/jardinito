package pl.edu.pb.jardinito.viewmodel

import AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.login(email, password)
                _uiState.value = AuthState.Success(response.message)
            } catch (e: Exception) {
                _uiState.value = AuthState.Error("Login failed")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.register(username, email, password)
                _uiState.value = AuthState.Success(response.message)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

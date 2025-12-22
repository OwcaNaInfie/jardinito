package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.LoginResponse
import pl.edu.pb.jardinito.data.model.RegisterRequest
import pl.edu.pb.jardinito.data.model.RegisterResponse
import pl.edu.pb.jardinito.data.remote.ApiService

class AuthViewModel(private val apiService: ApiService) : ViewModel() {

    // --- pola logowania ---
    var email: String = ""
    var password: String = ""
    // tymczasowy callback lub LiveData/StateFlow do obserwowania stanu logowania
    var loginResult: LoginResponse? = null
    // --- pola rejestracji ---
    var registerUsername: String = ""
    var registerEmail: String = ""
    var registerPassword: String = ""
    // wynik rejestracji (tymczasowy, można później LiveData/StateFlow)
    var registerResult: RegisterResponse? = null

    fun register() {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    email = registerEmail,
                    password = registerPassword,
                    username = registerUsername
                )
                registerResult = apiService.register(request)
            } catch (e: Exception) {
                e.printStackTrace()
                registerResult = null
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            try {
                val request = LoginRequest(email, password)
                loginResult = apiService.login(request)
            } catch (e: Exception) {
                e.printStackTrace()
                loginResult = null
            }
        }
    }
}

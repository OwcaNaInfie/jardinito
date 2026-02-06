package pl.edu.pb.jardinito.viewmodel

import AuthRepository
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.model.User
import pl.edu.pb.jardinito.ui.utils.validateEmail
import pl.edu.pb.jardinito.ui.utils.validatePassword
import pl.edu.pb.jardinito.data.model.RegisterFormState
import pl.edu.pb.jardinito.ui.utils.validateRepeatedPassword
import pl.edu.pb.jardinito.ui.utils.validateUsername
import retrofit2.HttpException

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState

    private var usernameJob: Job? = null
    private var emailJob: Job? = null
    private var passwordJob: Job? = null
    private var repeatedPasswordJob: Job? = null

    private inline fun updateForm(
        block: RegisterFormState.() -> RegisterFormState
    ) {
        _formState.update { it.block() }
    }

    fun testRegister400() {
        viewModelScope.launch {
            try {
                repository.register(
                    username = "",
                    email = "test@test.com",
                    password = "Haslo123!"
                )
            } catch (e: HttpException) {
                Log.d("REGISTER_TEST", "Status: ${e.code()}")
                Log.d("REGISTER_TEST", "Body: ${e.response()?.errorBody()?.string()}")
            }
        }
    }


    fun onUsernameChanged(username: String) {
        updateForm { copy(username = username, usernameTouched = true) }

        usernameJob?.cancel()
        usernameJob = viewModelScope.launch {
            delay(500)
            val frontendError = validateUsername(username)

            updateForm {
                copy(
                    username = username,
                    usernameError = frontendError,
                    usernameIsValid = frontendError == null
                )
            }

            if (frontendError == null) {
                checkUsername(username)
            }
        }
    }

    fun onEmailChanged(email: String) {
        updateForm { copy(email = email, emailTouched = true) }

        emailJob?.cancel()
        emailJob = viewModelScope.launch {
            delay(500)
            val frontendError = validateEmail(email)

            updateForm {
                copy(
                    email = email,
                    emailError = frontendError,
                    emailIsValid = frontendError == null
                )
            }

            if (frontendError == null) {
                checkEmail(email)
            }
        }
    }

    fun onPasswordChanged(password: String) {
        updateForm { copy(password = password, passwordTouched = true) }

        passwordJob?.cancel()
        passwordJob = viewModelScope.launch {
            delay(500)
            val frontendError = validatePassword(password)

            updateForm {
                copy(
                    password = password,
                    passwordTouched = true,
                    passwordError = frontendError,
                    passwordIsValid = frontendError == null
                )
            }
        }
    }

    fun onRepeatedPasswordChanged(repeated: String) {
        updateForm { copy(repeatedPassword = repeated, repeatedPasswordTouched = true) }

        repeatedPasswordJob?.cancel()
        repeatedPasswordJob = viewModelScope.launch {
            delay(500)
        val password = _formState.value.password
        val frontendError = validateRepeatedPassword(password, repeated)

        updateForm {
            copy(
                repeatedPassword = repeated,
                repeatedPasswordError = frontendError,
                repeatedPasswordIsValid = frontendError == null
            )
        }
            }
    }

    private fun checkUsername(username: String) {
        usernameJob?.cancel()
        usernameJob = viewModelScope.launch {
            val available = repository.isUsernameAvailable(username)

            if (!available) {
                updateForm {
                    copy(
                        usernameError = R.string.username_taken,
                    )
                }
            } else {
                updateForm {
                    copy(usernameIsValid = true)
                }
            }
        }
    }

    private fun checkEmail(email: String) {
        emailJob?.cancel()
        emailJob = viewModelScope.launch {
            val available = repository.isEmailAvailable(email)

            if (!available) {
                updateForm {
                    copy(
                        emailError = R.string.email_taken
                    )
                }
            } else {
                updateForm {
                    copy(emailIsValid = true)
                }
            }
        }
    }

    // =====================
    // AUTH ACTIONS
    // =====================

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

                _currentUser.value = User(
                    username = username,
                    email = userEmail
                )

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
                Log.d("REGISTER_RESPONSE", response.toString())
                val usernameResp = response.username
                val userEmail = response.email

                if (usernameResp == null || userEmail == null) {
                    _uiState.value = AuthState.Error("Invalid server response")
                    return@launch
                }

                _currentUser.value = User(
                    username = usernameResp,
                    email = userEmail
                )

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

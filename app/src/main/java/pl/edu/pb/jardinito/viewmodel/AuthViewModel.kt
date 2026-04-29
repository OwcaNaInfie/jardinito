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
import pl.edu.pb.jardinito.data.model.Avatar
import pl.edu.pb.jardinito.data.model.LoginFormState
import pl.edu.pb.jardinito.data.model.User
import pl.edu.pb.jardinito.ui.utils.validateEmail
import pl.edu.pb.jardinito.ui.utils.validatePassword
import pl.edu.pb.jardinito.data.model.RegisterFormState
import pl.edu.pb.jardinito.ui.utils.validateIsBlank
import pl.edu.pb.jardinito.ui.utils.validateRepeatedPassword
import retrofit2.HttpException
import pl.edu.pb.jardinito.viewmodel.state.AuthState
import pl.edu.pb.jardinito.viewmodel.state.UserState

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _registerFormState = MutableStateFlow(RegisterFormState())
    val registerFormState: StateFlow<RegisterFormState> = _registerFormState

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    private var usernameJob: Job? = null
    private var emailJob: Job? = null
    private var passwordJob: Job? = null
    private var repeatedPasswordJob: Job? = null

    private inline fun updateRegisterForm(
        block: RegisterFormState.() -> RegisterFormState
    ) {
        _registerFormState.update { it.block() }
    }

    private inline fun updateLoginForm(
        block: LoginFormState.() -> LoginFormState
    ) {
        _loginFormState.update { it.block() }
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
        updateRegisterForm { copy(username = username, usernameTouched = true) }

        usernameJob?.cancel()
        usernameJob = viewModelScope.launch {
            delay(500)
            val frontendError = validateIsBlank(username)

            updateRegisterForm {
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
        updateRegisterForm { copy(email = email, emailTouched = true) }

        emailJob?.cancel()
        emailJob = viewModelScope.launch {
            delay(500)
            val frontendError = validateEmail(email)

            updateRegisterForm {
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
        updateRegisterForm { copy(password = password, passwordTouched = true) }

        passwordJob?.cancel()
        passwordJob = viewModelScope.launch {
            delay(500)
            val frontendError = validatePassword(password)

            updateRegisterForm {
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
        updateRegisterForm { copy(repeatedPassword = repeated, repeatedPasswordTouched = true) }

        repeatedPasswordJob?.cancel()
        repeatedPasswordJob = viewModelScope.launch {
            delay(500)
        val password = _registerFormState.value.password
        val frontendError = validateRepeatedPassword(password, repeated)

        updateRegisterForm {
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
                updateRegisterForm {
                    copy(
                        usernameError = R.string.username_taken,
                    )
                }
            } else {
                updateRegisterForm {
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
                updateRegisterForm {
                    copy(
                        emailError = R.string.email_taken
                    )
                }
            } else {
                updateRegisterForm {
                    copy(emailIsValid = true)
                }
            }
        }
    }

    fun onLoginIdentifierChanged(value: String) {
        updateLoginForm {
            copy(
                loginIdentifier = value,
                loginIdentifierError = null,
                serverError = null
            )
        }
    }

    fun onLoginPasswordChanged(value: String) {
        updateLoginForm {
            copy(
                loginPassword = value,
                loginPasswordError = null,
                serverError = null
            )
        }
    }

    fun submitLogin() {

        val identifier = loginFormState.value.loginIdentifier.trim()
        val password = loginFormState.value.loginPassword

        val loginIdentifierBlankError = validateIsBlank(identifier)
        val loginPasswordBlankError = validateIsBlank(password)

        if (loginIdentifierBlankError != null || loginPasswordBlankError != null) {
            updateLoginForm {
                copy(
                    loginIdentifierError = loginIdentifierBlankError,
                    loginPasswordError = loginPasswordBlankError
                )
            }
            return
        }

        login(identifier, password)
    }


    // =====================
    // AUTH ACTIONS
    // =====================

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.login(identifier, password)
                val userId = response.userId
                val username = response.username
                val userEmail = response.email
                val avatar = response.avatar

                if (userId == null || username == null || userEmail == null || avatar == null) {
                    _uiState.value = AuthState.Error(R.string.error_invalid_response)
                    return@launch
                }

                _currentUser.value = User(
                    userId = userId,
                    username = username,
                    email = userEmail,
                    avatar = avatar
                )

                _uiState.value = AuthState.Success(response.message)

            } catch (e: HttpException) {
                val errorRes = when (e.code()) {
                    401 -> R.string.error_invalid_credentials
                    else -> R.string.error_server
                }
                _uiState.value = AuthState.Error(errorRes)
                updateLoginForm { copy(serverError = errorRes) }
            } catch (e: Exception) {
                val errorRes = R.string.error_server
                _uiState.value = AuthState.Error(errorRes)
                updateLoginForm { copy(serverError = errorRes) }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.googleLogin(idToken)

                val userId = response.userId
                val username = response.username
                val userEmail = response.email
                val avatar = response.avatar

                if (userId == null || username == null || userEmail == null || avatar == null) {
                    _uiState.value = AuthState.Error(R.string.error_invalid_response)
                    return@launch
                }

                _currentUser.value = User(
                    userId = userId,
                    username = username,
                    email = userEmail,
                    avatar = avatar
                )

                _uiState.value = AuthState.Success(response.message)

            } catch (e: Exception) {
                _uiState.value = AuthState.Error(R.string.error_google_login)
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val response = repository.register(username, email, password)
                Log.d("REGISTER_RESPONSE", response.toString())

                val userId = response.userId
                val username = response.username
                val userEmail = response.email
                val avatar = response.avatar

                if (userId == null || username == null || userEmail == null || avatar == null) {
                    _uiState.value = AuthState.Error(R.string.error_invalid_response)
                    return@launch
                }

                _currentUser.value = User(
                    userId = userId,
                    username = username,
                    email = userEmail,
                    avatar = avatar
                )

                _uiState.value = AuthState.Success(response.message)

            } catch (e: HttpException) {
                _uiState.value = when (e.code()) {
                    409 -> AuthState.Error(R.string.error_invalid_credentials)
                    else -> AuthState.Error(R.string.error_server)
                }
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(R.string.error_registration)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _uiState.value = AuthState.Idle
    }

    // Callback from UserViewModel
    fun updateAvatar(avatar: Avatar) {
        _currentUser.update { it?.copy(avatar = avatar) }
    }

    fun clearUserSession() {
        _currentUser.value = null
        _uiState.value = AuthState.Idle
    }
}

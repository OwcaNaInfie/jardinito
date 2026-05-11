package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.manager.AuthSessionManager
import pl.edu.pb.jardinito.data.model.Avatar
import pl.edu.pb.jardinito.data.model.LoginFormState
import pl.edu.pb.jardinito.data.model.RegisterFormState
import pl.edu.pb.jardinito.data.model.User
import pl.edu.pb.jardinito.data.repository.AuthRepository
import pl.edu.pb.jardinito.ui.utils.validateEmail
import pl.edu.pb.jardinito.ui.utils.validateIsBlank
import pl.edu.pb.jardinito.ui.utils.validatePassword
import pl.edu.pb.jardinito.ui.utils.validateRepeatedPassword
import pl.edu.pb.jardinito.viewmodel.state.AuthState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: AuthSessionManager
) : ViewModel() {

    // =====================
    // SESSION STATE (z AuthSessionManager)
    // =====================

    val uiState: StateFlow<AuthState> = sessionManager.uiState
    val currentUser: StateFlow<User?> = sessionManager.currentUser

    // =====================
    // FORM STATE
    // =====================

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    private val _registerFormState = MutableStateFlow(RegisterFormState())
    val registerFormState: StateFlow<RegisterFormState> = _registerFormState

    // =====================
    // JOBS
    // =====================

    private var usernameJob: Job? = null
    private var emailJob: Job? = null
    private var passwordJob: Job? = null
    private var repeatedPasswordJob: Job? = null

    // =====================
    // FORM HELPERS
    // =====================

    private inline fun updateLoginForm(block: LoginFormState.() -> LoginFormState) {
        _loginFormState.update { it.block() }
    }

    private inline fun updateRegisterForm(block: RegisterFormState.() -> RegisterFormState) {
        _registerFormState.update { it.block() }
    }

    private fun launchedWithDelay(job: Job?, block: suspend () -> Unit): Job {
        job?.cancel()
        return viewModelScope.launch {
            delay(500)
            block()
        }
    }

    // =====================
    // LOGIN FORM
    // =====================

    fun onLoginIdentifierChanged(value: String) {
        updateLoginForm { copy(loginIdentifier = value, loginIdentifierError = null, serverError = null) }
    }

    fun onLoginPasswordChanged(value: String) {
        updateLoginForm { copy(loginPassword = value, loginPasswordError = null, serverError = null) }
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
    // REGISTER FORM
    // =====================

    fun onUsernameChanged(username: String) {
        updateRegisterForm { copy(username = username, usernameTouched = true) }

        usernameJob?.cancel()
        usernameJob = viewModelScope.launch {
            delay(500)
            val frontendError = validateIsBlank(username)
            updateRegisterForm {
                copy(username = username, usernameError = frontendError, usernameIsValid = frontendError == null)
            }
            if (frontendError == null) checkUsername(username)
        }
    }

    fun onEmailChanged(email: String) {
        updateRegisterForm { copy(email = email, emailTouched = true) }

        emailJob?.cancel()
        emailJob = viewModelScope.launch {
            delay(500)
            val frontendError = validateEmail(email)
            updateRegisterForm {
                copy(email = email, emailError = frontendError, emailIsValid = frontendError == null)
            }
            if (frontendError == null) checkEmail(email)
        }
    }

    fun onPasswordChanged(password: String) {
        updateRegisterForm { copy(password = password, passwordTouched = true) }
        passwordJob = launchedWithDelay(passwordJob) {
            val error = validatePassword(password)
            updateRegisterForm { copy(passwordError = error, passwordIsValid = error == null) }
        }
    }

    fun onRepeatedPasswordChanged(repeated: String) {
        updateRegisterForm { copy(repeatedPassword = repeated, repeatedPasswordTouched = true) }
        repeatedPasswordJob = launchedWithDelay(repeatedPasswordJob) {
            val error = validateRepeatedPassword(_registerFormState.value.password, repeated)
            updateRegisterForm { copy(repeatedPasswordError = error, repeatedPasswordIsValid = error == null) }
        }
    }

    private fun checkUsername(username: String) {
        usernameJob?.cancel()
        usernameJob = viewModelScope.launch {
            val available = repository.isUsernameAvailable(username)
            if (!available) {
                updateRegisterForm { copy(usernameError = R.string.username_taken) }
            } else {
                updateRegisterForm { copy(usernameIsValid = true) }
            }
        }
    }

    private fun checkEmail(email: String) {
        emailJob?.cancel()
        emailJob = viewModelScope.launch {
            val available = repository.isEmailAvailable(email)
            if (!available) {
                updateRegisterForm { copy(emailError = R.string.email_taken) }
            } else {
                updateRegisterForm { copy(emailIsValid = true) }
            }
        }
    }

    // =====================
    // AUTH ACTIONS
    // =====================

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            sessionManager.setUiState(AuthState.Loading)
            try {
                val response = repository.login(identifier, password)
                val userId = response.userId
                val username = response.username
                val userEmail = response.email
                val avatar = response.avatar

                if (userId == null || username == null || userEmail == null || avatar == null) {
                    sessionManager.setUiState(AuthState.Error(R.string.error_invalid_response))
                    return@launch
                }

                sessionManager.setCurrentUser(User(userId = userId, username = username, email = userEmail, avatar = avatar))
                sessionManager.setUiState(AuthState.Success(response.message))

            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> {
                        val errorRes = R.string.error_invalid_credentials
                        sessionManager.setUiState(AuthState.Error(errorRes))
                        updateLoginForm { copy(serverError = errorRes) }
                    }
                    403 -> {
                        viewModelScope.launch {
                            try {
                                val result = repository.getUserId(identifier)
                                sessionManager.setPendingUserId(result.userId)
                                sessionManager.setPendingEmail(result.email)
                                sessionManager.setUiState(AuthState.UnverifiedAccount)
                            } catch (e: Exception) {
                                sessionManager.setUiState(AuthState.Error(R.string.error_server))
                            }
                        }
                    }
                    else -> {
                        val errorRes = R.string.error_server
                        sessionManager.setUiState(AuthState.Error(errorRes))
                        updateLoginForm { copy(serverError = errorRes) }
                    }
                }
            } catch (e: Exception) {
                val errorRes = R.string.error_server
                sessionManager.setUiState(AuthState.Error(errorRes))
                updateLoginForm { copy(serverError = errorRes) }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            sessionManager.setUiState(AuthState.Loading)
            try {
                val response = repository.googleLogin(idToken)
                val userId = response.userId
                val username = response.username
                val userEmail = response.email
                val avatar = response.avatar

                if (userId == null || username == null || userEmail == null || avatar == null) {
                    sessionManager.setUiState(AuthState.Error(R.string.error_invalid_response))
                    return@launch
                }

                sessionManager.setCurrentUser(User(userId = userId, username = username, email = userEmail, avatar = avatar))
                sessionManager.setUiState(AuthState.Success(response.message))

            } catch (e: Exception) {
                sessionManager.setUiState(AuthState.Error(R.string.error_google_login))
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            sessionManager.setUiState(AuthState.Loading)
            try {
                val response = repository.register(username, email, password)
                val userId = response.userId
                val username = response.username
                val userEmail = response.email
                val avatar = response.avatar

                if (userId == null || username == null || userEmail == null || avatar == null) {
                    sessionManager.setUiState(AuthState.Error(R.string.error_invalid_response))
                    return@launch
                }

                sessionManager.setPendingUserId(userId)
                sessionManager.setPendingEmail(userEmail)
                sessionManager.setUiState(AuthState.VerificationRequired)

            } catch (e: HttpException) {
                sessionManager.setUiState(when (e.code()) {
                    409 -> AuthState.Error(R.string.error_invalid_credentials)
                    else -> AuthState.Error(R.string.error_server)
                })
            } catch (e: Exception) {
                sessionManager.setUiState(AuthState.Error(R.string.error_registration))
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun clearUserSession() {
        sessionManager.clearSession()
    }

    fun resetUiState() {
        sessionManager.resetUiState()
    }

    fun updateAvatar(avatar: Avatar) {
        sessionManager.updateCurrentUser { it?.copy(avatar = avatar) }
    }

    fun updateUsername(username: String) {
        sessionManager.updateCurrentUser { it?.copy(username = username) }
    }

    fun updateEmail(email: String) {
        sessionManager.updateCurrentUser { it?.copy(email = email) }
    }
}

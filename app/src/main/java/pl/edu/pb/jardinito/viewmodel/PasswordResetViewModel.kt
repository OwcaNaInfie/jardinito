package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.manager.AuthSessionManager
import pl.edu.pb.jardinito.data.model.auth.ResetPasswordFormState
import pl.edu.pb.jardinito.data.repository.AuthRepository
import pl.edu.pb.jardinito.ui.utils.validatePassword
import pl.edu.pb.jardinito.ui.utils.validateRepeatedPassword
import pl.edu.pb.jardinito.viewmodel.state.AuthState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: AuthSessionManager
) : ViewModel() {

    val uiState = sessionManager.uiState

    // =====================
    // PENDING STATE
    // =====================

    private val _pendingResetPasswordUserId = MutableStateFlow<String?>(null)
    val pendingResetPasswordUserId: StateFlow<String?> = _pendingResetPasswordUserId

    private val _pendingResetIdentifier = MutableStateFlow("")
    val pendingResetIdentifier: StateFlow<String> = _pendingResetIdentifier

    // =====================
    // FORM STATE
    // =====================

    private val _resetPasswordFormState = MutableStateFlow(ResetPasswordFormState())
    val resetPasswordFormState: StateFlow<ResetPasswordFormState> = _resetPasswordFormState

    private var resetPasswordJob: Job? = null
    private var resetRepeatedPasswordJob: Job? = null

    private inline fun updateResetPasswordForm(block: ResetPasswordFormState.() -> ResetPasswordFormState) {
        _resetPasswordFormState.update { it.block() }
    }

    private fun launchedWithDelay(job: Job?, block: suspend () -> Unit): Job {
        job?.cancel()
        return viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            block()
        }
    }

    // =====================
    // FORM ACTIONS
    // =====================

    fun onResetCodeChanged(code: String) {
        updateResetPasswordForm { copy(code = code, codeTouched = true) }
    }

    fun onResetPasswordChanged(password: String) {
        updateResetPasswordForm { copy(newPassword = password, newPasswordTouched = true) }
        resetPasswordJob = launchedWithDelay(resetPasswordJob) {
            val error = validatePassword(password)
            updateResetPasswordForm { copy(newPasswordError = error, newPasswordIsValid = error == null) }
        }
    }

    fun onResetRepeatedPasswordChanged(repeated: String) {
        updateResetPasswordForm { copy(repeatedPassword = repeated, repeatedPasswordTouched = true) }
        resetRepeatedPasswordJob = launchedWithDelay(resetRepeatedPasswordJob) {
            val error = validateRepeatedPassword(_resetPasswordFormState.value.newPassword, repeated)
            updateResetPasswordForm { copy(repeatedPasswordError = error, repeatedPasswordIsValid = error == null) }
        }
    }

    fun resetPasswordFormClear() {
        _resetPasswordFormState.value = ResetPasswordFormState()
    }

    fun resetPasswordFlow() {
        _pendingResetPasswordUserId.value = null
        _pendingResetIdentifier.value = ""
    }

    // =====================
    // PASSWORD RESET ACTIONS
    // =====================

    fun forgotPassword(identifier: String) {
        viewModelScope.launch {
            sessionManager.setUiState(AuthState.Loading)
            if (identifier.isNotBlank()) {
                _pendingResetIdentifier.value = identifier
            }
            try {
                val response = repository.forgotPassword(_pendingResetIdentifier.value)
                _pendingResetPasswordUserId.value = response.userId
                sessionManager.setUiState(AuthState.PasswordResetRequired)
            } catch (e: HttpException) {
                sessionManager.setUiState(when (e.code()) {
                    403 -> AuthState.Error(R.string.error_email_not_verified)
                    400 -> AuthState.Error(R.string.error_google_account)
                    422 -> AuthState.Error(R.string.validator_blank)
                    else -> AuthState.Error(R.string.error_server)
                })
            } catch (e: Exception) {
                sessionManager.setUiState(AuthState.Error(R.string.error_server))
            }
        }
    }

    fun resetPassword(code: String, newPassword: String) {
        val userId = _pendingResetPasswordUserId.value ?: return

        viewModelScope.launch {
            sessionManager.setUiState(AuthState.Loading)
            try {
                repository.resetPassword(userId, code, newPassword)
                _pendingResetPasswordUserId.value = null
                sessionManager.setUiState(AuthState.PasswordResetSuccess)
            } catch (e: HttpException) {
                sessionManager.setUiState(when (e.code()) {
                    400, 404 -> AuthState.Error(R.string.error_invalid_code)
                    410 -> AuthState.Error(R.string.error_code_expired)
                    else -> AuthState.Error(R.string.error_server)
                })
            } catch (e: Exception) {
                sessionManager.setUiState(AuthState.Error(R.string.error_server))
            }
        }
    }

    fun resetUiState() {
        sessionManager.resetUiState()
    }
}
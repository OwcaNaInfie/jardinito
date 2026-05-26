package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.manager.AuthSessionManager
import pl.edu.pb.jardinito.data.model.profile.User
import pl.edu.pb.jardinito.data.repository.AuthRepository
import pl.edu.pb.jardinito.viewmodel.state.AuthState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: AuthSessionManager
) : ViewModel() {

    val pendingEmail = sessionManager.pendingEmail
    val uiState = sessionManager.uiState

    fun verifyEmail(code: String) {
        val userId = sessionManager.pendingUserId.value ?: return

        viewModelScope.launch {
            sessionManager.setUiState(AuthState.Loading)
            try {
                val response = repository.verifyEmail(userId, code)
                val username = response.username
                val userEmail = response.email
                val avatar = response.avatar

                if (username == null || userEmail == null || avatar == null) {
                    sessionManager.setUiState(AuthState.Error(R.string.error_invalid_response))
                    return@launch
                }

                sessionManager.setCurrentUser(User(userId = userId, username = username, email = userEmail, avatar = avatar))
                sessionManager.clearPendingData()
                sessionManager.setUiState(AuthState.Success(response.message))

            } catch (e: HttpException) {
                sessionManager.setUiState(when (e.code()) {
                    400 -> AuthState.Error(R.string.error_invalid_code)
                    410 -> AuthState.Error(R.string.error_code_expired)
                    else -> AuthState.Error(R.string.error_server)
                })
            } catch (e: Exception) {
                sessionManager.setUiState(AuthState.Error(R.string.error_server))
            }
        }
    }

    fun resendVerification() {
        val userId = sessionManager.pendingUserId.value ?: run {
            sessionManager.setUiState(AuthState.Error(R.string.error_server))
            return
        }

        viewModelScope.launch {
            sessionManager.setUiState(AuthState.Loading)
            try {
                repository.resendVerification(userId)
            } catch (e: Exception) {
                sessionManager.setUiState(AuthState.Error(R.string.error_server))
            }
        }
    }

    fun resetUiState() {
        sessionManager.resetUiState()
    }
}
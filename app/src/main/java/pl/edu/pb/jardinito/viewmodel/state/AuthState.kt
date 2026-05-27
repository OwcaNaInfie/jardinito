package pl.edu.pb.jardinito.viewmodel.state

import androidx.annotation.StringRes

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object VerificationRequired : AuthState()
    object UnverifiedAccount : AuthState()
    object PasswordResetRequired : AuthState()
    object PasswordResetSuccess : AuthState()
    object SessionRestored : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(@StringRes val messageRes: Int) : AuthState()
}

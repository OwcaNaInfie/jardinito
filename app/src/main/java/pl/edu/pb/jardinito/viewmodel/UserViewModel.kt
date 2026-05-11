package pl.edu.pb.jardinito.viewmodel

import android.content.Context
import android.net.Uri
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
import pl.edu.pb.jardinito.data.model.Avatar
import pl.edu.pb.jardinito.data.model.ProfileFormState
import pl.edu.pb.jardinito.data.repository.AuthRepository
import pl.edu.pb.jardinito.data.repository.UserRepository
import pl.edu.pb.jardinito.ui.utils.validateEmail
import pl.edu.pb.jardinito.ui.utils.validateIsBlank
import pl.edu.pb.jardinito.viewmodel.state.UserState
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // =====================
    // STATE
    // =====================

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    // =====================
    // PENDING STATE
    // =====================

    private val _pendingEmailChangeUserId = MutableStateFlow<String?>(null)
    val pendingEmailChangeUserId: StateFlow<String?> = _pendingEmailChangeUserId

    // =====================
    // FORM STATE
    // =====================

    private val _profileFormState = MutableStateFlow(ProfileFormState())
    val profileFormState: StateFlow<ProfileFormState> = _profileFormState

    // =====================
    // JOBS
    // =====================

    private var usernameCheckJob: Job? = null
    private var emailCheckJob: Job? = null

    // =====================
    // FORM HELPERS
    // =====================

    private inline fun updateProfileForm(block: ProfileFormState.() -> ProfileFormState) {
        _profileFormState.update { it.block() }
    }

    private fun launchedWithDelay(job: Job?, block: suspend () -> Unit): Job {
        job?.cancel()
        return viewModelScope.launch {
            delay(500)
            block()
        }
    }

    // =====================
    // PROFILE FORM ACTIONS
    // =====================

    fun onProfileUsernameChanged(username: String, currentUsername: String) {
        updateProfileForm { copy(username = username, usernameTouched = true, usernameError = null) }
        usernameCheckJob = launchedWithDelay(usernameCheckJob) {
            val blankError = validateIsBlank(username)
            when {
                blankError != null -> updateProfileForm {
                    copy(usernameError = blankError, usernameIsValid = false)
                }
                username != currentUsername -> {
                    val available = authRepository.isUsernameAvailable(username)
                    updateProfileForm {
                        copy(
                            usernameError = if (!available) R.string.username_taken else null,
                            usernameIsValid = available
                        )
                    }
                }
                else -> updateProfileForm { copy(usernameIsValid = true) }
            }
        }
    }

    fun onProfileEmailChanged(email: String, currentEmail: String) {
        updateProfileForm { copy(email = email, emailTouched = true, emailError = null) }
        emailCheckJob = launchedWithDelay(emailCheckJob) {
            val blankError = validateIsBlank(email)
            val formatError = if (blankError == null) validateEmail(email) else null
            when {
                blankError != null -> updateProfileForm {
                    copy(emailError = blankError, emailIsValid = false)
                }
                formatError != null -> updateProfileForm {
                    copy(emailError = formatError, emailIsValid = false)
                }
                email != currentEmail -> {
                    val available = authRepository.isEmailAvailable(email)
                    updateProfileForm {
                        copy(
                            emailError = if (!available) R.string.email_taken else null,
                            emailIsValid = available
                        )
                    }
                }
                else -> updateProfileForm { copy(emailIsValid = true) }
            }
        }
    }

    fun clearProfileForm() {
        _profileFormState.value = ProfileFormState()
    }

    // =====================
    // AVATAR ACTIONS
    // =====================

    fun uploadAvatar(userId: String, imageUri: Uri, context: Context, onSuccess: (Avatar) -> Unit) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val response = repository.uploadAvatar(userId, imageUri, context)
                onSuccess(Avatar(
                    default = response.avatar.default,
                    custom = response.avatar.custom,
                    google = response.avatar.google
                ))
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun deleteAvatar(userId: String, onSuccess: (Avatar) -> Unit) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val response = repository.deleteAvatar(userId)
                onSuccess(Avatar(
                    default = response.avatar.default,
                    custom = response.avatar.custom,
                    google = response.avatar.google
                ))
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Delete failed")
            }
        }
    }

    // =====================
    // USER DATA ACTIONS
    // =====================

    fun updateUsername(userId: String, username: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val response = repository.updateUsername(userId, username)
                onSuccess(response.username)
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Update username failed")
            }
        }
    }

    fun requestEmailChange(userId: String, newEmail: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                repository.requestEmailChange(userId, newEmail)
                _pendingEmailChangeUserId.value = userId
                _userState.value = UserState.Success
                onSuccess()
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Request email change failed")
            }
        }
    }

    fun confirmEmailChange(code: String, onSuccess: (String) -> Unit) {
        val userId = _pendingEmailChangeUserId.value ?: return

        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val response = repository.confirmEmailChange(userId, code)
                _pendingEmailChangeUserId.value = null
                onSuccess(response.email)
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Confirm email change failed")
            }
        }
    }

    fun clearPendingEmailChange() {
        _pendingEmailChangeUserId.value = null
    }

    // =====================
    // ACCOUNT ACTIONS
    // =====================

    fun deleteAccount(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                repository.deleteAccount(userId)
                _userState.value = UserState.Success
                onSuccess()
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Delete account failed")
            }
        }
    }

    // =====================
    // HELPERS
    // =====================

    fun resetState() {
        _userState.value = UserState.Idle
    }
}
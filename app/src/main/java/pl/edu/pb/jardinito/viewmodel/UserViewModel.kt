package pl.edu.pb.jardinito.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.model.Avatar
import pl.edu.pb.jardinito.data.repository.UserRepository
import pl.edu.pb.jardinito.viewmodel.state.UserState

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

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

    fun resetState() {
        _userState.value = UserState.Idle
    }
}
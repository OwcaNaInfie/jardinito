package pl.edu.pb.jardinito.viewmodel

sealed class AvatarUploadState {
    object Idle : AvatarUploadState()
    object Loading : AvatarUploadState()
    object Success : AvatarUploadState()
    data class Error(val message: String) : AvatarUploadState()
}
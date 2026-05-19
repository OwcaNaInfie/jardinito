package pl.edu.pb.jardinito.data.repository

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.edu.pb.jardinito.data.remote.MessageResponse
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.data.remote.UserApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    private val api = RetrofitInstance.user

    suspend fun uploadAvatar(userId: String, imageUri: Uri, context: Context): UserApiService.AvatarUploadResponse {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")
        val bytes = inputStream.readBytes()
        inputStream.close()

        val imagePart = MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "avatar.jpg",
            body = bytes.toRequestBody("image/jpeg".toMediaType())
        )
        val userIdPart = userId.toRequestBody("text/plain".toMediaType())

        return api.uploadAvatar(imagePart, userIdPart)
    }

    suspend fun deleteAvatar(userId: String): UserApiService.AvatarUploadResponse {
        return api.deleteAvatar(UserApiService.DeleteAvatarRequest(userId))
    }

    suspend fun deleteAccount(userId: String): MessageResponse {
        return api.deleteAccount(UserApiService.DeleteAccountRequest(userId))
    }

    suspend fun updateUsername(userId: String, username: String): UserApiService.UpdateUsernameResponse {
        return api.updateUsername(UserApiService.UpdateUsernameRequest(userId, username))
    }

    suspend fun requestEmailChange(userId: String, newEmail: String): MessageResponse {
        return api.requestEmailChange(UserApiService.RequestEmailChangeRequest(userId, newEmail))
    }

    suspend fun confirmEmailChange(userId: String, code: String): UserApiService.ConfirmEmailChangeResponse {
        return api.confirmEmailChange(UserApiService.ConfirmEmailChangeRequest(userId, code))
    }
}
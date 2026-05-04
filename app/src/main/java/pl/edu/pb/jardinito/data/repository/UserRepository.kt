package pl.edu.pb.jardinito.data.repository

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.edu.pb.jardinito.data.remote.ApiService
import pl.edu.pb.jardinito.data.remote.RetrofitInstance

class UserRepository {

    suspend fun uploadAvatar(userId: String, imageUri: Uri, context: Context): ApiService.AvatarUploadResponse {
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

        return RetrofitInstance.api.uploadAvatar(imagePart, userIdPart)
    }

    suspend fun deleteAvatar(userId: String): ApiService.AvatarUploadResponse {
        return RetrofitInstance.api.deleteAvatar(ApiService.DeleteAvatarRequest(userId))
    }

    suspend fun deleteAccount(userId: String): ApiService.MessageResponse {
        return RetrofitInstance.api.deleteAccount(ApiService.DeleteAccountRequest(userId))
    }
}
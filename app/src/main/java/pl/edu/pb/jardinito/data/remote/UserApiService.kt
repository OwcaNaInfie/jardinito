package pl.edu.pb.jardinito.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserApiService {

    @Multipart
    @POST("api/user/upload-avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part,
        @Part("userId") userId: RequestBody
    ): AvatarUploadResponse

    @POST("api/user/delete-avatar")
    suspend fun deleteAvatar(@Body request: DeleteAvatarRequest): AvatarUploadResponse

    @POST("api/user/delete-account")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): MessageResponse

    @POST("api/user/update-username")
    suspend fun updateUsername(@Body request: UpdateUsernameRequest): UpdateUsernameResponse

    @POST("api/user/request-email-change")
    suspend fun requestEmailChange(@Body request: RequestEmailChangeRequest): MessageResponse

    @POST("api/user/confirm-email-change")
    suspend fun confirmEmailChange(@Body request: ConfirmEmailChangeRequest): ConfirmEmailChangeResponse

    data class DeleteAccountRequest(val userId: String)
    data class DeleteAvatarRequest(val userId: String)
    data class AvatarUploadResponse(val avatar: AvatarDto)
    data class AvatarDto(val default: String, val custom: String? = null, val google: String? = null)
    data class UpdateUsernameRequest(val userId: String, val username: String)
    data class UpdateUsernameResponse(val message: String, val username: String)
    data class RequestEmailChangeRequest(val userId: String, val newEmail: String)
    data class ConfirmEmailChangeRequest(val userId: String, val code: String)
    data class ConfirmEmailChangeResponse(val message: String, val email: String)
}
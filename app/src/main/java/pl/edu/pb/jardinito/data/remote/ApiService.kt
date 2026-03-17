package pl.edu.pb.jardinito.data.remote

import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): AuthResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @Multipart
    @POST("api/auth/upload-avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part,
        @Part("userId") userId: RequestBody
    ): AvatarUploadResponse

    @POST("api/auth/delete-avatar")
    suspend fun deleteAvatar(
        @Body request: DeleteAvatarRequest
    ): AvatarUploadResponse

//    Register Form Validation
@GET("api/auth/check-username")
    suspend fun checkUsername(@Query("username") username: String): UsernameAvailabilityResponse

@GET("api/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): EmailAvailabilityResponse
}

data class UsernameAvailabilityResponse(
    val usernameAvailable: Boolean
)

data class EmailAvailabilityResponse(
    val emailAvailable: Boolean
)

data class AvatarUploadResponse(
    val avatar: AvatarDto
)

data class AvatarDto(
    val default: String,
    val custom: String? = null,
    val google: String? = null
)

data class DeleteAvatarRequest(
    val userId: String
)


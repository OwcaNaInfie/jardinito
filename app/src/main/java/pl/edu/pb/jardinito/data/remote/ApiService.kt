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
    // Auth and verification
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): AuthResponse

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): AuthResponse

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(
        @Body request: ResendVerificationRequest
    ): MessageResponse

    data class VerifyEmailRequest(
        val userId: String,
        val code: String
    )

    data class ResendVerificationRequest(
        val userId: String
    )

    @POST("api/auth/get-user-id")
    suspend fun getUserId(
        @Body request: GetUserIdRequest
    ): GetUserIdResponse

    data class GetUserIdRequest(
        val identifier: String
    )

    data class GetUserIdResponse(
        val userId: String,
        val email: String,
        val isVerified: Boolean
    )

    // Reset password
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): ForgotPasswordResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): MessageResponse

    data class ForgotPasswordRequest(
        val identifier: String
    )

    data class ForgotPasswordResponse(
        val message: String,
        val userId: String?
    )

    data class ResetPasswordRequest(
        val userId: String,
        val code: String,
        val newPassword: String
    )

    // Profile
    @Multipart
    @POST("api/user/upload-avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part,
        @Part("userId") userId: RequestBody
    ): AvatarUploadResponse

    @POST("api/user/delete-avatar")
    suspend fun deleteAvatar(
        @Body request: DeleteAvatarRequest
    ): AvatarUploadResponse

    @POST("api/user/delete-account")
    suspend fun deleteAccount(
        @Body request: DeleteAccountRequest
    ): MessageResponse

    data class DeleteAccountRequest(
        val userId: String
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

    data class MessageResponse(
        val message: String
    )

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
package pl.edu.pb.jardinito.data.remote

import pl.edu.pb.jardinito.data.model.auth.AuthResponse
import pl.edu.pb.jardinito.data.model.auth.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.auth.LoginRequest
import pl.edu.pb.jardinito.data.model.auth.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): AuthResponse

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): AuthResponse

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): MessageResponse

    @POST("api/auth/get-user-id")
    suspend fun getUserId(@Body request: GetUserIdRequest): GetUserIdResponse

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): MessageResponse

    @GET("api/auth/check-username")
    suspend fun checkUsername(@Query("username") username: String): UsernameAvailabilityResponse

    @GET("api/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): EmailAvailabilityResponse

    data class VerifyEmailRequest(val userId: String, val code: String)
    data class ResendVerificationRequest(val userId: String)
    data class GetUserIdRequest(val identifier: String)
    data class GetUserIdResponse(val userId: String, val email: String, val isVerified: Boolean)
    data class ForgotPasswordRequest(val identifier: String)
    data class ForgotPasswordResponse(val message: String, val userId: String?)
    data class ResetPasswordRequest(val userId: String, val code: String, val newPassword: String)
    data class UsernameAvailabilityResponse(val usernameAvailable: Boolean)
    data class EmailAvailabilityResponse(val emailAvailable: Boolean)
}
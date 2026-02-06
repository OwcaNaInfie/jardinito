package pl.edu.pb.jardinito.data.remote

import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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


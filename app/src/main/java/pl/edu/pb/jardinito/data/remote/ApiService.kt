package pl.edu.pb.jardinito.data.remote

import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse
}


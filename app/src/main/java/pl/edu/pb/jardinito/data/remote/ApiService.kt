package pl.edu.pb.jardinito.data.remote

import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.LoginResponse
import pl.edu.pb.jardinito.data.model.RegisterRequest
import pl.edu.pb.jardinito.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}


import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.RegisterRequest

class AuthRepository {

    suspend fun login(email: String, password: String): AuthResponse {
        return RetrofitInstance.api.login(
            LoginRequest(email, password)
        )
    }

    suspend fun register(username: String, email: String, password: String): AuthResponse {
        return RetrofitInstance.api.register(
            RegisterRequest(username, email, password)
        )
    }
}


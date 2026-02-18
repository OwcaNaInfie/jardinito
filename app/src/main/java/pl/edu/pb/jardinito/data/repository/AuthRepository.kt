import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.RegisterRequest

class AuthRepository {

    //    Register Form Validation
    suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            RetrofitInstance.api.checkUsername(username).usernameAvailable
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun isEmailAvailable(email: String): Boolean {
        return try {
            RetrofitInstance.api.checkEmail(email).emailAvailable
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun login(identifier: String, password: String): AuthResponse {
        return RetrofitInstance.api.login(
            LoginRequest(identifier, password)
        )
    }

    suspend fun googleLogin(idToken: String): AuthResponse {
        return RetrofitInstance.api.googleLogin(
            GoogleLoginRequest(idToken)
        )
    }

    suspend fun register(username: String, email: String, password: String): AuthResponse {
        return RetrofitInstance.api.register(
            RegisterRequest(username, email, password)
        )
    }
}


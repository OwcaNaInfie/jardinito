import android.util.Log
import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.RegisterRequest
import pl.edu.pb.jardinito.data.remote.ApiService
import pl.edu.pb.jardinito.data.remote.RetrofitInstance

class AuthRepository {

    //    Register Form Validation
    suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            val result = RetrofitInstance.api.checkUsername(username).usernameAvailable
            result
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isEmailAvailable(email: String): Boolean {
        return try {
            val result = RetrofitInstance.api.checkEmail(email).emailAvailable
            result
        } catch (e: Exception) {
            false
        }
    }

    // Auth and verification
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

    suspend fun verifyEmail(userId: String, code: String): AuthResponse {
        return RetrofitInstance.api.verifyEmail(ApiService.VerifyEmailRequest(userId, code))
    }

    suspend fun resendVerification(userId: String): ApiService.MessageResponse {
        return RetrofitInstance.api.resendVerification(ApiService.ResendVerificationRequest(userId))
    }

    suspend fun getUserId(identifier: String): ApiService.GetUserIdResponse {
        return RetrofitInstance.api.getUserId(ApiService.GetUserIdRequest(identifier))
    }
}


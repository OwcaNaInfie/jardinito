package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.RegisterRequest
import pl.edu.pb.jardinito.data.remote.ApiService
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    private val api = RetrofitInstance.api

    //    Register Form Validation
    suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            val result = api.checkUsername(username).usernameAvailable
            result
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isEmailAvailable(email: String): Boolean {
        return try {
            val result = api.checkEmail(email).emailAvailable
            result
        } catch (e: Exception) {
            false
        }
    }

    // Auth and verification
    suspend fun login(identifier: String, password: String): AuthResponse {
        return api.login(
            LoginRequest(identifier, password)
        )
    }

    suspend fun googleLogin(idToken: String): AuthResponse {
        return api.googleLogin(
            GoogleLoginRequest(idToken)
        )
    }

    suspend fun register(username: String, email: String, password: String): AuthResponse {
        return api.register(
            RegisterRequest(username, email, password)
        )
    }

    suspend fun verifyEmail(userId: String, code: String): AuthResponse {
        return api.verifyEmail(ApiService.VerifyEmailRequest(userId, code))
    }

    suspend fun resendVerification(userId: String): ApiService.MessageResponse {
        return api.resendVerification(ApiService.ResendVerificationRequest(userId))
    }

    suspend fun getUserId(identifier: String): ApiService.GetUserIdResponse {
        return api.getUserId(ApiService.GetUserIdRequest(identifier))
    }

    suspend fun forgotPassword(identifier: String): ApiService.ForgotPasswordResponse {
        return api.forgotPassword(ApiService.ForgotPasswordRequest(identifier))
    }

    suspend fun resetPassword(userId: String, code: String, newPassword: String): ApiService.MessageResponse {
        return api.resetPassword(ApiService.ResetPasswordRequest(userId, code, newPassword))
    }
}


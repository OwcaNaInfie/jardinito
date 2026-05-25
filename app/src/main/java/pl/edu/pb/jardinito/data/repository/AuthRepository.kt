package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.model.auth.AuthResponse
import pl.edu.pb.jardinito.data.model.auth.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.auth.LoginRequest
import pl.edu.pb.jardinito.data.model.auth.RegisterRequest
import pl.edu.pb.jardinito.data.remote.MessageResponse
import pl.edu.pb.jardinito.data.remote.AuthApiService
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    private val api = RetrofitInstance.auth

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
        return api.verifyEmail(AuthApiService.VerifyEmailRequest(userId, code))
    }

    suspend fun resendVerification(userId: String): MessageResponse {
        return api.resendVerification(AuthApiService.ResendVerificationRequest(userId))
    }

    suspend fun getUserId(identifier: String): AuthApiService.GetUserIdResponse {
        return api.getUserId(AuthApiService.GetUserIdRequest(identifier))
    }

    suspend fun forgotPassword(identifier: String): AuthApiService.ForgotPasswordResponse {
        return api.forgotPassword(AuthApiService.ForgotPasswordRequest(identifier))
    }

    suspend fun resetPassword(userId: String, code: String, newPassword: String): MessageResponse {
        return api.resetPassword(AuthApiService.ResetPasswordRequest(userId, code, newPassword))
    }
}
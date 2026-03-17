import pl.edu.pb.jardinito.data.model.AuthResponse
import pl.edu.pb.jardinito.data.model.GoogleLoginRequest
import pl.edu.pb.jardinito.data.model.LoginRequest
import pl.edu.pb.jardinito.data.model.RegisterRequest
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.edu.pb.jardinito.data.remote.AvatarUploadResponse

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

    suspend fun uploadAvatar(userId: String, imageUri: Uri, context: Context): AvatarUploadResponse {
        val contentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")

        val bytes = inputStream.readBytes()
        inputStream.close()

        val imagePart = MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "avatar.jpg",
            body = bytes.toRequestBody("image/jpeg".toMediaType())
        )

        val userIdPart = userId.toRequestBody("text/plain".toMediaType())

        return RetrofitInstance.api.uploadAvatar(imagePart, userIdPart)
    }
}


package pl.edu.pb.jardinito.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(

    @SerializedName("message")
    val message: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("userId")
    val userId: String?,

    @SerializedName("username")
    val username: String?
)

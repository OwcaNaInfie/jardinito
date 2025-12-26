package pl.edu.pb.jardinito.data.model

data class AuthResponse(
    val message: String,
    val email: String?,
    val userId: String?
)

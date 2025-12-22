package pl.edu.pb.jardinito.data.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)


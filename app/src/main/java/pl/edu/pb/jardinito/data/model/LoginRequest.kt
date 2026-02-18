package pl.edu.pb.jardinito.data.model

data class LoginRequest(
    val identifier: String,
    val password: String
)
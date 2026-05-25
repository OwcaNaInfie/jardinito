package pl.edu.pb.jardinito.data.model.auth

data class LoginRequest(
    val identifier: String,
    val password: String
)
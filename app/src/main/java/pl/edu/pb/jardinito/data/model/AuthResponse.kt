package pl.edu.pb.jardinito.data.model

import pl.edu.pb.jardinito.model.Avatar

data class AuthResponse(
    val message: String,
    val userId: String?,
    val username: String?,
    val email: String?,
    val avatar: Avatar?
)

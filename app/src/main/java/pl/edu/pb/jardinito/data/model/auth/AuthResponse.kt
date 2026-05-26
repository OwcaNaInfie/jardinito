package pl.edu.pb.jardinito.data.model.auth

import pl.edu.pb.jardinito.data.model.profile.Avatar

data class AuthResponse(
    val message: String,
    val userId: String?,
    val username: String?,
    val email: String?,
    val avatar: Avatar?,
    val isVerified: Boolean? = null
)

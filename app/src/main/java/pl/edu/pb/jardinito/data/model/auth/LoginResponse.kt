package pl.edu.pb.jardinito.data.model.auth

import pl.edu.pb.jardinito.data.model.profile.Avatar

data class LoginResponse(
    val token: String,
    val message: String,
    val userId: String?,
    val avatar: Avatar?
)
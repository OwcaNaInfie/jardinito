package pl.edu.pb.jardinito.data.model

import pl.edu.pb.jardinito.model.Avatar

data class LoginResponse(
    val token: String,
    val message: String,
    val userId: String?,
    val avatar: Avatar?
)
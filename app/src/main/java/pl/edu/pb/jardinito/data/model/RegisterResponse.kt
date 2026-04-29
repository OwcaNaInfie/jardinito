package pl.edu.pb.jardinito.data.model

import pl.edu.pb.jardinito.data.model.Avatar

data class RegisterResponse(
    val token: String,
    val message: String,
    val userId: String?,
    val avatar: Avatar?
)

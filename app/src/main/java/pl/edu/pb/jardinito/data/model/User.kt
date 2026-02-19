package pl.edu.pb.jardinito.model

data class Avatar(
    val type: String,
    val value: String
)

data class User(
    val userId: String,
    val username: String,
    val email: String,
    val avatar: Avatar
)


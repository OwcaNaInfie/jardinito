package pl.edu.pb.jardinito.data.model.profile

data class User(
    val userId: String,
    val username: String,
    val email: String,
    val avatar: Avatar
)


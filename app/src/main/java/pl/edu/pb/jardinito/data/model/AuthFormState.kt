package pl.edu.pb.jardinito.data.model

data class AuthFormState(
    val email: String = "",
    val emailError: Int? = null,

    val password: String = "",
    val passwordError: Int? = null,

    val repeatedPassword: String = "",
    val repeatedPasswordError: Int? = null
)

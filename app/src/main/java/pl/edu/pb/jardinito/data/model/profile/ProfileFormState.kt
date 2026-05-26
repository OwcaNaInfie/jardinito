package pl.edu.pb.jardinito.data.model.profile

data class ProfileFormState(
    val username: String = "",
    val usernameTouched: Boolean = false,
    val usernameError: Int? = null,
    val usernameIsValid: Boolean = false,

    val email: String = "",
    val emailTouched: Boolean = false,
    val emailError: Int? = null,
    val emailIsValid: Boolean = false
)
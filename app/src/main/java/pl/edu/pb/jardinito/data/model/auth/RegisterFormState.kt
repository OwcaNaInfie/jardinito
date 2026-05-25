package pl.edu.pb.jardinito.data.model.auth

data class RegisterFormState(
    val username: String = "",
    val usernameTouched: Boolean = false,
    val usernameError: Int? = null,
    val usernameIsValid: Boolean = false,

    val email: String = "",
    val emailTouched: Boolean = false,
    val emailError: Int? = null,
    val emailIsValid: Boolean = false,

    val password: String = "",
    val passwordTouched: Boolean = false,
    val passwordError: Int? = null,
    val passwordIsValid: Boolean = false,

    val repeatedPassword: String = "",
    val repeatedPasswordTouched: Boolean = false,
    val repeatedPasswordError: Int? = null,
    val repeatedPasswordIsValid: Boolean = false
)


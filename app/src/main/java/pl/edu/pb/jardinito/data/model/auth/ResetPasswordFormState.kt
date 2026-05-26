package pl.edu.pb.jardinito.data.model.auth

data class ResetPasswordFormState(
    val code: String = "",
    val codeTouched: Boolean = false,
    val codeError: Int? = null,

    val newPassword: String = "",
    val newPasswordTouched: Boolean = false,
    val newPasswordError: Int? = null,
    val newPasswordIsValid: Boolean = false,

    val repeatedPassword: String = "",
    val repeatedPasswordTouched: Boolean = false,
    val repeatedPasswordError: Int? = null,
    val repeatedPasswordIsValid: Boolean = false
)
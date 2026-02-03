package pl.edu.pb.jardinito.ui.utils

import android.util.Patterns
import androidx.annotation.StringRes
import pl.edu.pb.jardinito.R

private val PASSWORD_REGEX =
    Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")

@StringRes
fun validateEmail(email: String): Int? {
    if (email.isBlank()) return R.string.validator_email_blank
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return R.string.validator_email_invalid
    return null
}

@StringRes
fun validatePassword(password: String): Int? {
    if (password.isBlank())
        return R.string.validator_password_blank

    if (!PASSWORD_REGEX.matches(password))
        return R.string.validator_password_regex

    if (password.length < 8)
        return R.string.validator_password_lenght
    return null
}

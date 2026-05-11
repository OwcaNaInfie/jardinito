package pl.edu.pb.jardinito.ui.utils

import android.util.Patterns
import androidx.annotation.StringRes
import pl.edu.pb.jardinito.R

private val PASSWORD_REGEX =
    Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")

@StringRes
fun validateIsBlank(input: String): Int? {
    if (input.isBlank()) return R.string.validator_blank
    return null
}

@StringRes
fun validateUsername(username: String): Int? {
    validateIsBlank(username)
    if (username.length > 20) return R.string.validator_username_too_long
    if (Patterns.EMAIL_ADDRESS.matcher(username).matches())
        return R.string.validator_username_email_pattern
    return null
}

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
    if (password.length < 8)
        return R.string.validator_password_length
    if (!PASSWORD_REGEX.matches(password))
        return R.string.validator_password_regex
    return null
}

@StringRes
fun validateRepeatedPassword(password: String, repeatedPassword: String): Int? {
    if (repeatedPassword.isBlank())
        return R.string.validator_repeated_password_blank
    if (repeatedPassword != password)
        return R.string.validator_repeated_password_invalid
    return null
}

fun validateVerificationCode(input: String): Boolean {
    return input.length <= 6 && input.all { it.isDigit() }
}

package pl.edu.pb.jardinito.ui.utils

fun validateEmail(email: String): String? {
    if (email.isBlank()) return "Email is required"
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return "Invalid email address"
    return null
}

fun validatePassword(password: String): String? {
    if (password.isBlank()) return "Password is required"
    if (password.length < 8)
        return "Password must be at least 8 characters"
    return null
}

package pl.edu.pb.jardinito.data.model.profile

data class Avatar(
    val default: String,
    val custom: String? = null,
    val google: String? = null
) {
    // Returns the correct URL to display based on priority
    fun activeValue(): String {
        return custom ?: google ?: default
    }

    fun activeType(): String {
        return when {
            custom != null -> "custom"
            google != null -> "google"
            else -> "default"
        }
    }
}
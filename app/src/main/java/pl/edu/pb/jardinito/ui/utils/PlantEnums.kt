package pl.edu.pb.jardinito.ui.utils

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import pl.edu.pb.jardinito.R

// ============================================================================
// Enums
// ============================================================================

enum class PlantColor(
    @StringRes val labelRes: Int,
    val color: Color
) {
    RED(R.string.plant_color_red, Color(0xFFE53935)),
    PINK(R.string.plant_color_pink, Color(0xFFEC407A)),
    YELLOW(R.string.plant_color_yellow, Color(0xFFFDD835)),
    WHITE(R.string.plant_color_white, Color(0xFFF5F5F5)),
    PURPLE(R.string.plant_color_purple, Color(0xFF8E24AA)),
    ORANGE(R.string.plant_color_orange, Color(0xFFFB8C00)),
    BLUE(R.string.plant_color_blue, Color(0xFF1E88E5));

    companion object {
        fun fromKey(key: String): PlantColor? =
            entries.firstOrNull { it.name == key.uppercase() }
    }
}

enum class PlantSize(@StringRes val labelRes: Int) {
    SMALL(R.string.plant_size_small),
    MEDIUM(R.string.plant_size_medium),
    LARGE(R.string.plant_size_large);

    companion object {
        fun fromKey(key: String): PlantSize? =
            entries.firstOrNull { it.name == key.uppercase() }
    }
}

enum class PlantOwnershipStatus(@StringRes val labelRes: Int) {
    ALL(R.string.filter_status_all),
    UNLOCKED(R.string.filter_status_unlocked),
    LOCKED(R.string.filter_status_locked)
}

enum class PriceSortOrder(@StringRes val labelRes: Int, val arrow: String) {
    ASCENDING(R.string.filter_price_ascending, "↑"),
    DESCENDING(R.string.filter_price_descending, "↓")
}

// ============================================================================
// Extension Functions
// ============================================================================

@JvmName("plantColorSetToChipLabelRes")
fun Set<PlantColor>.toChipLabelRes(): Int? =
    if (size == 1) first().labelRes else null

@JvmName("plantSizeSetToChipLabelRes")
fun Set<PlantSize>.toChipLabelRes(): Int? =
    if (size == 1) first().labelRes else null

fun PriceSortOrder?.toChipLabel(base: String): String =
    this?.let { "$base ${it.arrow}" } ?: base

fun PlantOwnershipStatus?.isActiveFilter(): Boolean =
    this != null && this != PlantOwnershipStatus.ALL

fun PlantOwnershipStatus?.toChipLabelRes(): Int? =
    if (isActiveFilter()) this?.labelRes else null
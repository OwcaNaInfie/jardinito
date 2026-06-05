package pl.edu.pb.jardinito.ui.utils

import pl.edu.pb.jardinito.data.model.Plant

fun List<Plant>.filterByColors(colors: Set<PlantColor>): List<Plant> =
    if (colors.isEmpty()) this
    else filter { plant ->
        colors.any { color -> plant.colors.any { c -> c.uppercase() == color.name } }
    }

fun List<Plant>.filterBySizes(sizes: Set<PlantSize>): List<Plant> =
    if (sizes.isEmpty()) this
    else filter { plant -> sizes.any { size -> plant.size.uppercase() == size.name } }

fun List<Plant>.filterByName(
    query: String,
    resolvedNames: Map<String, String> = emptyMap()
): List<Plant> =
    if (query.isBlank()) this
    else filter {
        val name = resolvedNames[it.plantId] ?: it.name
        name.contains(query.trim(), ignoreCase = true)
    }

fun List<Plant>.filterByStatus(
    status: PlantOwnershipStatus?,
    unlockedIds: Set<String>
): List<Plant> = when (status) {
    null, PlantOwnershipStatus.ALL -> this
    PlantOwnershipStatus.UNLOCKED -> filter { it.plantId in unlockedIds }
    PlantOwnershipStatus.LOCKED   -> filter { it.plantId !in unlockedIds }
}

fun List<Plant>.sortedByPriceOrder(order: PriceSortOrder?): List<Plant> = when (order) {
    null                     -> this
    PriceSortOrder.ASCENDING  -> sortedBy { it.price }
    PriceSortOrder.DESCENDING -> sortedByDescending { it.price }
}

// Combined
fun List<Plant>.applyFilters(
    query: String = "",
    colors: Set<PlantColor> = emptySet(),
    sizes: Set<PlantSize> = emptySet(),
    status: PlantOwnershipStatus? = null,
    unlockedIds: Set<String> = emptySet(),
    priceSortOrder: PriceSortOrder? = null,
    resolvedNames: Map<String, String> = emptyMap()
): List<Plant> = this
    .filterByName(query, resolvedNames)
    .filterByColors(colors)
    .filterBySizes(sizes)
    .filterByStatus(status, unlockedIds)
    .sortedByPriceOrder(priceSortOrder)
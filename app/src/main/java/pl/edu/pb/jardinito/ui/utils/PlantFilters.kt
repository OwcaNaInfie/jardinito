package pl.edu.pb.jardinito.ui.utils

import pl.edu.pb.jardinito.data.model.Plant

fun List<Plant>.filterByColor(color: PlantColor?): List<Plant> =
    if (color == null) this
    else filter { it.colors.any { c -> c.uppercase() == color.name } }

fun List<Plant>.filterBySize(size: PlantSize?): List<Plant> =
    if (size == null) this
    else filter { it.size.uppercase() == size.name }

fun List<Plant>.filterByPrice(maxPrice: Int?): List<Plant> =
    if (maxPrice == null) this
    else filter { it.price <= maxPrice }

fun List<Plant>.applyFilters(
    color: PlantColor? = null,
    size: PlantSize? = null,
    maxPrice: Int? = null
): List<Plant> = this
    .filterByColor(color)
    .filterBySize(size)
    .filterByPrice(maxPrice)
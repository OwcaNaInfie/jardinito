package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.model.PlantImages
import pl.edu.pb.jardinito.data.model.PlantWitheredImages
import pl.edu.pb.jardinito.data.remote.PlantApiService
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepository @Inject constructor() {

    private val api = RetrofitInstance.plants

    // In-memory cache — wypełniany przy getPlants() lub pojedynczych getPlant().
    // Singleton gwarantuje jeden cache na cały czas życia aplikacji.
    private val cache = mutableMapOf<String, Plant>()

    suspend fun getPlants(): List<Plant> {
        return api.getPlants().plants.map { dto ->
            dto.toModel().also { cache[it.plantId] = it }
        }
    }

    // internal — dostępne w całym module (data/repository).
    // SessionRepository używa tego przez `with(plantRepository) { dto.plantId.toModel() }`.
    internal fun PlantApiService.PlantDto.toModel() = Plant(
        plantId = _id,
        name = name,
        nameKey = nameKey,
        descriptionKey = descriptionKey,
        colors = colors,
        size = size,
        images = PlantImages(
            small = images.small,
            medium = images.medium,
            mediumOutlined = images.mediumOutlined,
            large = images.large
        ),
        witheredImages = PlantWitheredImages(
            small = witheredImages.small,
            medium = witheredImages.medium,
            mediumOutlined = witheredImages.mediumOutlined
        ),
        minDurationDev = minDurationDev,
        minDuration = minDuration,
        price = price
    )
}
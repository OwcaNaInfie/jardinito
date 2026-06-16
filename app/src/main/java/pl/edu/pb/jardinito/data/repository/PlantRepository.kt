package pl.edu.pb.jardinito.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val cache = mutableMapOf<String, Plant>()
    private val fetchMutex = Mutex()

    suspend fun getPlants(): List<Plant> {
        if (cache.isNotEmpty()) return cache.values.toList()
        return fetchMutex.withLock {
            if (cache.isNotEmpty()) cache.values.toList()
            else api.getPlants().plants.map { dto ->
                dto.toModel().also { cache[it.plantId] = it }
            }
        }
    }

    fun getPlantById(plantId: String): Plant? = cache[plantId]

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
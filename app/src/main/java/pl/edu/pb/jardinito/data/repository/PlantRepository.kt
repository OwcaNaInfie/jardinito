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

    suspend fun getPlants(): List<Plant> {
        return api.getPlants().plants.map { it.toModel() }
    }

    suspend fun getPlant(plantId: String): Plant {
        return api.getPlant(plantId).plant.toModel()
    }

    private fun PlantApiService.PlantDto.toModel() = Plant(
        plantId = _id,
        name = name,
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
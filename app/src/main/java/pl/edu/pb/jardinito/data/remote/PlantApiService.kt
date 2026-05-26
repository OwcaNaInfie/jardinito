package pl.edu.pb.jardinito.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface PlantApiService {

    @GET("api/plants")
    suspend fun getPlants(): PlantsResponse

    @GET("api/plants/{plantId}")
    suspend fun getPlant(@Path("plantId") plantId: String): PlantResponse

    data class PlantImagesDto(
        val small: String,
        val medium: String,
        val mediumOutlined: String,
        val large: String
    )

    data class PlantWitheredImagesDto(
        val small: String,
        val medium: String,
        val mediumOutlined: String
    )

    data class PlantDto(
        val _id: String,
        val name: String,
        val images: PlantImagesDto,
        val witheredImages: PlantWitheredImagesDto,
        val minDurationDev: Int,
        val minDuration: Int,
        val price: Int
    )

    data class PlantsResponse(val plants: List<PlantDto>)
    data class PlantResponse(val plant: PlantDto)
}
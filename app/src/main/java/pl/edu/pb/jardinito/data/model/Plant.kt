package pl.edu.pb.jardinito.data.model

data class Plant(
    val plantId: String,
    val name: String,
    val nameKey: String,
    val descriptionKey: String,
    val colors: List<String>,
    val size: String,
    val images: PlantImages,
    val witheredImages: PlantWitheredImages,
    val minDurationDev: Int,
    val minDuration: Int,
    val price: Int
)

data class PlantImages(
    val small: String,
    val medium: String,
    val mediumOutlined: String,
    val large: String
)

data class PlantWitheredImages(
    val small: String,
    val medium: String,
    val mediumOutlined: String
)
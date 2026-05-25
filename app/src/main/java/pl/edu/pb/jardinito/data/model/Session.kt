package pl.edu.pb.jardinito.data.model

data class Session(
    val sessionId: String,
    val userId: String,
    val plant: Plant,
    val tags: List<Tag>,
    val plannedDuration: Int,
    val actualDuration: Int?,
    val status: String,
    val coinsEarned: Int,
    val startedAt: String,
    val completedAt: String?
)
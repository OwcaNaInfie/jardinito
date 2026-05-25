package pl.edu.pb.jardinito.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SessionApiService {

    @POST("api/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): CreateSessionResponse

    @GET("api/sessions")
    suspend fun getSessions(
        @Query("userId") userId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("status") status: String? = null
    ): SessionsResponse

    data class TagSnapshotDto(
        val tagId: String,
        val name: String,
        val color: String
    )

    data class CreateSessionRequest(
        val userId: String,
        val plantId: String,
        val tags: List<TagSnapshotDto>,
        val plannedDuration: Int,
        val actualDuration: Int?,
        val status: String,
        val startedAt: String,
        val completedAt: String?
    )

    data class CreateSessionResponse(
        val session: SessionDto,
        val coinsEarned: Int
    )

    data class SessionDto(
        val _id: String,
        val userId: String,
        val plantId: PlantApiService.PlantDto,
        val tags: List<TagSnapshotDto>,
        val plannedDuration: Int,
        val actualDuration: Int?,
        val status: String,
        val coinsEarned: Int,
        val startedAt: String,
        val completedAt: String?
    )

    data class SessionsResponse(val sessions: List<SessionDto>)
}
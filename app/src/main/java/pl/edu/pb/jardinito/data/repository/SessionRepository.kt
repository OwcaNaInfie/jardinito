package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.data.remote.SessionApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val plantRepository: PlantRepository
) {

    private val api = RetrofitInstance.sessions

    suspend fun createSession(
        userId: String,
        plantId: String,
        tag: Tag?,
        plannedDuration: Int,
        actualDuration: Int?,
        status: String,
        startedAt: String,
        completedAt: String?
    ): SessionApiService.CreateSessionResponse {
        return api.createSession(
            SessionApiService.CreateSessionRequest(
                userId = userId,
                plantId = plantId,
                tag = tag?.let { SessionApiService.TagSnapshotDto(it.tagId, it.name, it.color) },
                plannedDuration = plannedDuration,
                actualDuration = actualDuration,
                status = status,
                startedAt = startedAt,
                completedAt = completedAt
            )
        )
    }

    // Używane przez StatisticsViewModel (dowolny zakres dat).
    // Naprawa N+1: dto.plantId to już pełny PlantDto z populate() — wywołujemy
    // toModel() bezpośrednio zamiast robić osobny GET /plants/:id dla każdej sesji.
    suspend fun getSessionsByDateRange(
        userId: String,
        from: String? = null,
        to: String? = null,
        status: String? = null
    ): List<Session> {
        return api.getSessionsByDateRange(userId, from, to, status).sessions.map { dto ->
            dto.toSession()
        }
    }

    // Używane przez GardenViewModel (preset: day/week/month).
    // Ta sama naprawa N+1 co w getSessionsByDateRange.
    suspend fun getSessionsByPreset(
        userId: String,
        period: String = "day"
    ): List<Session> {
        return api.getSessionsByPreset(userId, period).sessions.map { dto ->
            dto.toSession()
        }
    }

    suspend fun updateSessionTag(sessionId: String, tag: Tag?) {
        api.updateSessionTag(
            sessionId = sessionId,
            request = SessionApiService.UpdateSessionTagRequest(tag?.tagId)
        )
    }

    // Prywatna funkcja pomocnicza — mapuje SessionDto → Session korzystając
    // z internal toModel() z PlantRepository (przez with()) zamiast HTTP request.
    private fun SessionApiService.SessionDto.toSession(): Session =
        with(plantRepository) {
            Session(
                sessionId = _id,
                userId = userId,
                plant = plantId.toModel(),
                tag = tag?.let { Tag(tagId = it.tagId, name = it.name, color = it.color) },
                plannedDuration = plannedDuration,
                actualDuration = actualDuration,
                status = status,
                coinsEarned = coinsEarned,
                startedAt = startedAt,
                completedAt = completedAt
            )
        }
}
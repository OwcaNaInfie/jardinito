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

    suspend fun getSessions(
        userId: String,
        from: String? = null,
        to: String? = null,
        status: String? = null
    ): List<Session> {
        return api.getSessions(userId, from, to, status).sessions.map { dto ->
            Session(
                sessionId = dto._id,
                userId = dto.userId,
                plant = plantRepository.getPlant(dto.plantId._id),
                tag = dto.tag?.let { Tag(tagId = it.tagId, name = it.name, color = it.color) },
                plannedDuration = dto.plannedDuration,
                actualDuration = dto.actualDuration,
                status = dto.status,
                coinsEarned = dto.coinsEarned,
                startedAt = dto.startedAt,
                completedAt = dto.completedAt
            )
        }
    }
}
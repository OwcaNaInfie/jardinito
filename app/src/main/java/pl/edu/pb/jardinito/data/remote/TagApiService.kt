package pl.edu.pb.jardinito.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TagApiService {

    @GET("api/tags")
    suspend fun getTags(@Query("userId") userId: String): TagsResponse

    @POST("api/tags")
    suspend fun createTag(@Body request: CreateTagRequest): CreateTagResponse

    @PUT("api/tags/{tagId}")
    suspend fun updateTag(
        @Path("tagId") tagId: String,
        @Body request: UpdateTagRequest
    ): UpdateTagResponse

    @DELETE("api/tags/{tagId}")
    suspend fun deleteTag(
        @Path("tagId") tagId: String,
        @Query("userId") userId: String
    ): MessageResponse

    data class TagDto(val _id: String, val name: String, val color: String)
    data class TagsResponse(val tags: List<TagDto>)
    data class CreateTagRequest(val userId: String, val name: String, val color: String)
    data class CreateTagResponse(val tag: TagDto)
    data class UpdateTagRequest(val userId: String, val name: String, val color: String)
    data class UpdateTagResponse(val tag: TagDto)
}
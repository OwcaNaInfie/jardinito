package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.remote.TagApiService
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor() {

    private val api = RetrofitInstance.tags

    suspend fun getTags(userId: String): List<Tag> {
        return api.getTags(userId).tags.map {
            Tag(tagId = it._id, name = it.name, color = it.color)
        }
    }

    suspend fun createTag(userId: String, name: String, color: String): Tag {
        val response = api.createTag(TagApiService.CreateTagRequest(userId, name, color))
        return Tag(tagId = response.tag._id, name = response.tag.name, color = response.tag.color)
    }

    suspend fun updateTag(tagId: String, userId: String, name: String, color: String): Tag {
        val response = api.updateTag(tagId, TagApiService.UpdateTagRequest(userId, name, color))
        return Tag(tagId = response.tag._id, name = response.tag.name, color = response.tag.color)
    }

    suspend fun deleteTag(tagId: String, userId: String) {
        api.deleteTag(tagId, userId)
    }
}
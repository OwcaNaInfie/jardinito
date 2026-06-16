package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.repository.TagRepository
import pl.edu.pb.jardinito.viewmodel.state.TagState
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(
    private val repository: TagRepository
) : ViewModel() {

    // =====================
    // STATE
    // =====================

    private val _tagState = MutableStateFlow<TagState>(TagState.Idle)
    val tagState: StateFlow<TagState> = _tagState

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredTags = combine(_tags, _searchQuery) { tags, query ->
        if (query.isBlank()) tags
        else tags.filter { it.name.contains(query, ignoreCase = true) }
    }

    // =====================
    // ACTIONS
    // =====================

    fun loadTags(userId: String) {
        viewModelScope.launch {
            _tagState.value = TagState.Loading
            try {
                _tags.value = repository.getTags(userId)
                _tagState.value = TagState.Idle
            } catch (e: Exception) {
                _tagState.value = TagState.Error(e.message ?: "Failed to load tags")
            }
        }
    }

    fun createTag(userId: String, name: String, color: String) {
        viewModelScope.launch {
            _tagState.value = TagState.Loading
            try {
                val newTag = repository.createTag(userId, name, color)
                _tags.value = _tags.value + newTag
                _tagState.value = TagState.Idle
            } catch (e: Exception) {
                _tagState.value = TagState.Error(e.message ?: "Failed to create tag")
            }
        }
    }

    fun reorderTagsLocally(tagIds: List<String>) {
        val reordered = tagIds.mapNotNull { id -> _tags.value.find { it.tagId == id } }
        _tags.value = reordered
    }

    fun reorderTags(userId: String, tagIds: List<String>) {
        viewModelScope.launch {
            try {
                repository.reorderTags(userId, tagIds)
            } catch (e: Exception) {
                _tagState.value = TagState.Error(e.message ?: "Reorder failed")
            }
        }
    }

    fun updateTag(tagId: String, userId: String, name: String, color: String) {
        viewModelScope.launch {
            _tagState.value = TagState.Loading
            try {
                val updatedTag = repository.updateTag(tagId, userId, name, color)
                _tags.value = _tags.value.map { if (it.tagId == tagId) updatedTag else it }
                _tagState.value = TagState.Idle
            } catch (e: Exception) {
                _tagState.value = TagState.Error(e.message ?: "Failed to update tag")
            }
        }
    }

    fun deleteTag(tagId: String, userId: String) {
        viewModelScope.launch {
            _tagState.value = TagState.Loading
            try {
                android.util.Log.d("TagViewModel", "Deleting tag: $tagId for user: $userId")
                repository.deleteTag(tagId, userId)
                _tags.value = _tags.value.filter { it.tagId != tagId }
                android.util.Log.d("TagViewModel", "Tag deleted, remaining: ${_tags.value.size}")
                _tagState.value = TagState.Idle
            } catch (e: Exception) {
                android.util.Log.e("TagViewModel", "Delete failed: ${e.message}")
                _tagState.value = TagState.Error(e.message ?: "Failed to delete tag")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
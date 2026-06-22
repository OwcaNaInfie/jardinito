package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import pl.edu.pb.jardinito.ui.theme.Dimensions.roundedCorner_s
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.components.SearchInput
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.TagViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TagsScreen(
    tagViewModel: TagViewModel,
    userId: String,
    showAddTagDialog: Boolean,
    onAddTagDialogDismiss: () -> Unit
) {
    val filteredTags by tagViewModel.filteredTags.collectAsState(initial = emptyList())
    val searchQuery by tagViewModel.searchQuery.collectAsState()
    var tagToEdit by remember { mutableStateOf<Tag?>(null) }

    TagsScreenContent(
        filteredTags = filteredTags,
        searchQuery = searchQuery,
        onSearchQueryChanged = { tagViewModel.onSearchQueryChanged(it) },
        onTagClick = { tagToEdit = it },
        onReorderLocal = { tagIds -> tagViewModel.reorderTagsLocally(tagIds) },
        onReorderCommit = { tagIds -> tagViewModel.reorderTags(userId, tagIds) }
    )

    if (showAddTagDialog) {
        TagDialog(
            title = stringResource(R.string.tag_add_title),
            onConfirm = { name, color ->
                tagViewModel.createTag(userId, name, color)
                onAddTagDialogDismiss()
            },
            onDismiss = onAddTagDialogDismiss
        )
    }

    tagToEdit?.let { tag ->
        TagDialog(
            title = stringResource(R.string.tag_edit_title),
            initialName = tag.name,
            initialColor = tag.color,
            showDeleteButton = true,
            onConfirm = { name, color ->
                tagViewModel.updateTag(tag.tagId, userId, name, color)
                tagToEdit = null
            },
            onDelete = {
                tagViewModel.deleteTag(tag.tagId, userId)
                tagToEdit = null
            },
            onDismiss = { tagToEdit = null }
        )
    }
}

@Composable
fun TagsScreenContent(
    filteredTags: List<Tag>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onTagClick: (Tag) -> Unit,
    onReorderLocal: (List<String>) -> Unit,
    onReorderCommit: (List<String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
            .padding(top = Dimensions.topBarHeight, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchInput(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = stringResource(R.string.tags_search_placeholder)
        )

        if (filteredTags.isEmpty()) {
            TagsEmptyState()
        } else {
            TagsReorderableList(
                tags = filteredTags,
                onTagClick = onTagClick,
                onReorderLocal = onReorderLocal,
                onReorderCommit = onReorderCommit
            )
        }
    }
}

@Composable
fun TagsReorderableList(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    onReorderLocal: (List<String>) -> Unit,
    onReorderCommit: (List<String>) -> Unit
) {
    val lazyListState = rememberLazyListState()
    var pendingOrder by remember(tags) { mutableStateOf(tags.map { it.tagId }) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val mutable = pendingOrder.toMutableList()
        mutable.add(to.index, mutable.removeAt(from.index))
        pendingOrder = mutable
        onReorderLocal(mutable)  // aktualizacja UI bez requestu
    }

    LazyColumn(state = lazyListState) {
        items(tags.size, key = { tags[it].tagId }) { index ->
            val tag = tags[index]
            ReorderableItem(state = reorderableLazyListState, key = tag.tagId) { isDragging ->
                Column(
                    modifier = Modifier.then(
                        if (isDragging) Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, colors.primary300, RoundedCornerShape(8.dp))
                        else Modifier
                    )
                ) {
                    TagItem(
                        tag = tag,
                        onClick = { onTagClick(tag) },
                        modifier = Modifier.longPressDraggableHandle(
                            onDragStopped = {
                                onReorderCommit(pendingOrder)  // request dopiero tu
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TagsEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📌", style = MaterialTheme.typography.displaySmall)
            Text(
                text = stringResource(R.string.tags_empty_title),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.neutralGray
            )
        }
    }
}

@Composable
fun TagItem(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TagColors.colorCompose(tag.color)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.pushpin),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = tag.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun TagDialogHeader(
    title: String,
    showDeleteButton: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (showDeleteButton) {
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.tag_delete_title), tint = colors.error)
            }
        }
    }
}

@Composable
private fun TagColorPicker(
    colorPalette: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colorPalette.chunked(5).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowColors.forEach { colorKey ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TagColors.colorCompose(colorKey))
                            .clickable { onColorSelected(colorKey) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == colorKey) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagDialog(
    title: String,
    initialName: String = "",
    initialColor: String = TagColors.default,
    showDeleteButton: Boolean = false,
    onConfirm: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val colorPalette = TagColors.palette.keys.toList()

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(roundedCorner_s))
                .background(colors.primary50)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TagDialogHeader(
                title = title,
                showDeleteButton = showDeleteButton,
                onDeleteClick = { showDeleteConfirmDialog = true }
            )

            TextField(
                value = name,
                onValueChange = { if (it.length <= 15) name = it },
                placeholder = { Text(stringResource(R.string.tag_name_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.neutralLight,
                    unfocusedContainerColor = colors.neutralLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            TagColorPicker(
                colorPalette = colorPalette,
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(
                    text = stringResource(R.string.cancel),
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Primary,
                    onClick = onDismiss
                )
                Spacer(modifier = Modifier.padding(4.dp))
                AppButton(
                    text = stringResource(R.string.tag_save),
                    size = ButtonSize.Small,
                    variant = ButtonVariant.Secondary,
                    enabled = name.isNotBlank(),
                    onClick = { onConfirm(name, selectedColor) }
                )
            }
        }
    }

    if (showDeleteConfirmDialog) {
        ConfirmDialog(
            config = DialogConfig(
            title = stringResource(R.string.tag_delete_title),
            message = stringResource(R.string.tag_delete_message, name),
            confirmText = stringResource(R.string.tag_delete_title),
            variant = DialogVariant.Error,
            ),
            onConfirm = {
                showDeleteConfirmDialog = false
                onDelete?.invoke()
                onDismiss()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}
package pl.edu.pb.jardinito.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.ui.components.BasePickerSheet
import pl.edu.pb.jardinito.ui.components.PickerSheetContent
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun TagPickerDrawer(
    tags: List<Tag>,
    selectedTag: Tag?,
    onConfirm: (Tag?) -> Unit,
    onDismiss: () -> Unit
) {
    BasePickerSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.focus_pick_tags),
        containerColor = colors.neutralLight,
        content = PickerSheetContent.List { hideAndDismiss ->
            if (tags.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tags_empty_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.neutralGray
                    )
                }
            } else {
                tags.forEach { tag ->
                    TagPickerRow(
                        tag = tag,
                        isSelected = tag == selectedTag,
                        onToggle = {
                            onConfirm(if (selectedTag == tag) null else tag)
                            hideAndDismiss()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun TagPickerRow(
    tag: Tag,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) TagColors.colorCompose(tag.color).copy(alpha = 0.12f)
                else Color.Transparent
            )
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
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
            Text(text = tag.name, style = MaterialTheme.typography.bodyLarge)
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = TagColors.colorCompose(tag.color),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
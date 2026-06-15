package pl.edu.pb.jardinito.ui.screens.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.AppChip
import pl.edu.pb.jardinito.ui.components.AppChipVariant
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.DetailLayout
import pl.edu.pb.jardinito.ui.components.DialogConfig
import pl.edu.pb.jardinito.ui.components.DialogVariant
import pl.edu.pb.jardinito.ui.screens.focus.TagPickerDrawer
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.formatIdleTime
import pl.edu.pb.jardinito.ui.utils.formatSessionDate
import pl.edu.pb.jardinito.ui.utils.rememberPlantName

@Composable
fun SessionDetailScreen(
    session: Session,
    tags: List<Tag>,
    onBack: () -> Unit,
    onTagChange: (Tag?) -> Unit
) {
    val plantName = rememberPlantName(session.plant)
    var showTagPicker by remember { mutableStateOf(false) }
    var pendingTag by remember { mutableStateOf<Tag?>(null) }
    var showUpdateTagDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    DetailLayout(
        imageUrl = "${RetrofitInstance.BASE_URL}plants/${session.plant.images.large}",
        imageContentDescription = plantName,
        onClose = onBack
    ) {
        Text(
            text = plantName,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.neutralBlack
        )
        SessionStatus(session.status)
        Row(
            horizontalArrangement = Arrangement.spacedBy(itemsSpacing_xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppChip(
                text = session.tag?.name ?: stringResource(R.string.session_detail_no_tag),
                variant = session.tag?.let { AppChipVariant.Tinted(TagColors.colorCompose(it.color)) }
                    ?: AppChipVariant.Outlined,
                onClick = { showTagPicker = true }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(itemsSpacing_xs)) {
            Text(
                text = stringResource(R.string.session_detail_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.neutralBlack
            )
            SessionInfoRow(
                label = stringResource(R.string.session_detail_planned_duration),
                value = formatIdleTime(session.plannedDuration, devMode = true)
            )
            session.actualDuration?.let { actual ->
                SessionInfoRow(
                    label = stringResource(R.string.session_detail_actual_duration),
                    value = formatIdleTime(actual, devMode = true)
                )
            }
            SessionInfoRow(
                label = stringResource(R.string.session_detail_coins),
                value = "${session.coinsEarned}"
            )
            SessionInfoRow(
                label = stringResource(R.string.session_detail_started_at),
                value = formatSessionDate(session.startedAt)
            )
            session.completedAt?.let {
                SessionInfoRow(
                    label = stringResource(R.string.session_detail_completed_at),
                    value = formatSessionDate(it)
                )
            }
        }
    }

    if (showTagPicker) {
        TagPickerDrawer(
            tags = tags,
            selectedTag = session.tag,
            onConfirm = { newTag ->
                pendingTag = newTag
                showUpdateTagDialog = true
            },
            onDismiss = { showTagPicker = false }
        )
    }

    if (showUpdateTagDialog) {
        ConfirmDialog(
            config = DialogConfig(
                title = stringResource(R.string.dialog_tag_update_title),
                message = stringResource(R.string.dialog_tag_update_message),
                confirmText = stringResource(R.string.confirm),
                dismissText = stringResource(R.string.cancel),
                variant = DialogVariant.Info
            ),
            onConfirm = {
                onTagChange(pendingTag)
                showUpdateTagDialog = false
            },
            onDismiss = { showUpdateTagDialog = false }
        )
    }
}

// =====================
// PRIVATE COMPONENTS
// =====================

@Composable
private fun SessionStatus(status: String) {
    Text(
        text = status,
        style = MaterialTheme.typography.labelMedium,
        color = if (status == "failed") colors.error else colors.primary500
    )
}

@Composable
private fun SessionInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.neutralLightGray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = colors.neutralDark)
    }
}
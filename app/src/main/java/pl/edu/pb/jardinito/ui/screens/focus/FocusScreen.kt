package pl.edu.pb.jardinito.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.ConfirmDialog
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_m
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.rememberPlantName
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.viewmodel.FocusViewModel
import pl.edu.pb.jardinito.viewmodel.SessionResult
import pl.edu.pb.jardinito.viewmodel.TagViewModel
import pl.edu.pb.jardinito.viewmodel.state.TimerState

// =====================
// DATA CLASSES
// =====================

data class TimerUiState(
    val timerState: TimerState,
    val selectedDuration: Int,
    val remainingSeconds: Int,
    val progress: Float,
)

data class PlantUiState(
    val plants: List<Plant>,
    val selectedPlant: Plant?
)

data class TagUiState(
    val tags: List<Tag>,
    val selectedTag: Tag?
)

data class TimerActions(
    val onDurationChange: (Int) -> Unit,
    val onStart: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onStopClick: () -> Unit
)

data class TimerCallbacks(
    val onDurationChange: (Int) -> Unit,
    val onPlantClick: () -> Unit
)

data class TimerConfig(
    val devMode: Boolean = true,
    val minValue: Int = if (devMode) 5 else 15,
    val maxValue: Int = if (devMode) 60 else 120
) {
    companion object {
        fun forPlant(plant: Plant?, devMode: Boolean): TimerConfig {
            val base = TimerConfig(devMode = devMode)
            return base.copy(
                minValue = plant?.let {
                    if (devMode) it.minDurationDev else it.minDuration
                } ?: base.minValue
            )
        }
    }
}

data class TimerCanvasState(
    val isIdle: Boolean,
    val progress: Float,
    val selectionProgress: Float,
    val selectedDuration: Int,
    val minValue: Int
)

data class TimerCanvasColors(
    val selectionColor: Color,
    val timerColor: Color,
    val trackColor: Color
)

// =====================
// SCREEN
// =====================

@Composable
fun FocusScreen(
    focusViewModel: FocusViewModel,
    tagViewModel: TagViewModel,
    userId: String
) {
    val timerState by focusViewModel.timerState.collectAsState()
    val selectedDuration by focusViewModel.selectedDuration.collectAsState()
    val remainingSeconds by focusViewModel.remainingSeconds.collectAsState()
    val progress by focusViewModel.progress.collectAsState()
    val plants by focusViewModel.plants.collectAsState()
    val selectedPlant by focusViewModel.selectedPlant.collectAsState()
    val selectedTag by focusViewModel.selectedTag.collectAsState()
    val tags by tagViewModel.tags.collectAsState()
    val sessionResult by focusViewModel.sessionResult.collectAsState()
    val showStopConfirmDialog by focusViewModel.showStopConfirmDialog.collectAsState()
    val unlockedPlantIds by focusViewModel.unlockedPlantIds.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            tagViewModel.loadTags(userId)
            focusViewModel.loadUnlockedPlants(userId)
        }
    }

    val config = TimerConfig.forPlant(selectedPlant, focusViewModel.devMode)

    FocusScreenContent(
        timerUiState = TimerUiState(
            timerState = timerState,
            selectedDuration = selectedDuration,
            remainingSeconds = remainingSeconds,
            progress = progress
        ),
        plantUiState = PlantUiState(plants = plants, selectedPlant = selectedPlant),
        tagUiState = TagUiState(tags = tags, selectedTag = selectedTag),
        timerActions = TimerActions(
            onDurationChange = {
                if (config.devMode) focusViewModel.setDurationDev(it)
                else focusViewModel.setDuration(it)
            },
            onStart = { focusViewModel.start(userId) },
            onPause = { focusViewModel.pause() },
            onResume = { focusViewModel.resume(userId) },
            onStopClick = { focusViewModel.requestStop(userId) }
        ),
        config = config,
        onPlantSelected = { focusViewModel.selectPlant(it) },
        onTagSelected = { focusViewModel.selectTag(it) },
        sessionResult = sessionResult,
        showStopConfirmDialog = showStopConfirmDialog,
        onSessionResultDismissed = { focusViewModel.clearSessionResult() },
        onStopConfirmed = { focusViewModel.confirmStop(userId) },
        onStopDismissed = { focusViewModel.dismissStop(userId) },
        unlockedPlantIds = unlockedPlantIds,
    )
}

// =====================
// CONTENT
// =====================

@Composable
fun FocusScreenContent(
    timerUiState: TimerUiState,
    plantUiState: PlantUiState,
    tagUiState: TagUiState,
    timerActions: TimerActions,
    config: TimerConfig,
    onPlantSelected: (Plant) -> Unit,
    onTagSelected: (Tag?) -> Unit,
    sessionResult: SessionResult?,
    showStopConfirmDialog: Boolean,
    onSessionResultDismissed: () -> Unit,
    onStopConfirmed: () -> Unit,
    onStopDismissed: () -> Unit,
    unlockedPlantIds: Set<String>,

    ) {
    val isIdle = timerUiState.timerState is TimerState.Idle
    val isRunning = timerUiState.timerState is TimerState.Running
    val isPaused = timerUiState.timerState is TimerState.Paused
    var showPlantPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }

    val minutes = timerUiState.remainingSeconds / 60
    val seconds = timerUiState.remainingSeconds % 60
    val timeText = "%d:%02d".format(minutes, seconds)
    val unit = if (config.devMode) "s" else "min"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
            .padding(top = Dimensions.topBarHeight, bottom = 16.dp, start = screenPadding_s, end = screenPadding_s),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        CircularTimer(
            progress = timerUiState.progress,
            isIdle = isIdle,
            selectedDuration = timerUiState.selectedDuration,
            selectedPlant = plantUiState.selectedPlant,
            callbacks = TimerCallbacks(
                onDurationChange = timerActions.onDurationChange,
                onPlantClick = { if (isIdle) showPlantPicker = true }
            ),
            config = config
        )

        Text(
            text = if (isIdle) "${timerUiState.selectedDuration} $unit" else timeText,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.neutralGray,
        )

        SelectedTagChip(
            selectedTag = tagUiState.selectedTag,
            isIdle = isIdle,
            onClick = { if (isIdle) showTagPicker = true }
        )

        TimerControls(
            isIdle = isIdle,
            isRunning = isRunning,
            isPaused = isPaused,
            actions = timerActions
        )
    }

    if (showPlantPicker) {
        PlantPickerDrawer(
            plants = plantUiState.plants,
            selectedPlant = plantUiState.selectedPlant,
            unlockedPlantIds = unlockedPlantIds,
            onPlantSelected = onPlantSelected,
            onDismiss = { showPlantPicker = false }
        )
    }

    if (showTagPicker) {
        TagPickerDrawer(
            tags = tagUiState.tags,
            selectedTag = tagUiState.selectedTag,
            onConfirm = { selected ->
                onTagSelected(selected)
            },
            onDismiss = { showTagPicker = false }
        )
    }

    if (showStopConfirmDialog) {
        StopConfirmDialog(
            onConfirm = onStopConfirmed,
            onDismiss = onStopDismissed
        )
    }

    when (val result = sessionResult) {
        is SessionResult.Completed -> SessionCompletedDialog(
            result = result,
            onDismiss = onSessionResultDismissed
        )
        is SessionResult.Failed -> SessionFailedDialog(
            result = result,
            onDismiss = onSessionResultDismissed
        )
        null -> {}
    }
}

// =====================
// COMPONENTS
// =====================

@Composable
private fun TimerControls(
    isIdle: Boolean,
    isRunning: Boolean,
    isPaused: Boolean,
    actions: TimerActions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing_s, Alignment.CenterHorizontally)
    ) {
        when {
            isIdle -> AppButton(
                text = stringResource(R.string.focus_start),
                size = ButtonSize.Max,
                variant = ButtonVariant.Tertiary,
                onClick = actions.onStart
            )
            isRunning -> {
                AppButton(
                    iconVector = Icons.Default.Pause,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onPause
                )
                AppButton(
                    iconVector = Icons.Default.Stop,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onStopClick
                )
            }
            isPaused -> {
                AppButton(
                    iconVector = Icons.Default.PlayArrow,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onResume
                )
                AppButton(
                    iconVector = Icons.Default.Stop,
                    size = ButtonSize.Large,
                    circle = true,
                    variant = ButtonVariant.Primary,
                    onClick = actions.onStopClick
                )
            }
        }
    }
}

// =====================
// TAG COMPONENTS
// =====================

@Composable
private fun SelectedTagChip(
    selectedTag: Tag?,
    isIdle: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selectedTag == null) colors.primary100
                else TagColors.colorCompose(selectedTag.color).copy(alpha = 0.15f)
            )
            .then(if (isIdle) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedTag == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.pushpin),
                    contentDescription = null,
                    tint = colors.neutralGray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.focus_add_tag),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.neutralGray
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = TagColors.colorCompose(selectedTag.color),
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = selectedTag.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = TagColors.colorCompose(selectedTag.color)
                )
            }
        }
    }
}

// =====================
// DIALOGS
// =====================

@Composable
private fun StopConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        config = pl.edu.pb.jardinito.ui.components.DialogConfig(
            title = stringResource(R.string.session_stop_title),
            message = stringResource(R.string.session_stop_message),
            confirmText = stringResource(R.string.session_stop_confirm),
            variant = pl.edu.pb.jardinito.ui.components.DialogVariant.Warning
        ),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun SessionCompletedDialog(
    result: SessionResult.Completed,
    onDismiss: () -> Unit
) {
    val imageUrl = rememberSvgImageRequest("${RetrofitInstance.BASE_URL}plants/${result.plant.images.medium}")
    ConfirmDialog(
        config = pl.edu.pb.jardinito.ui.components.DialogConfig(
            title = stringResource(R.string.session_completed_title),
            message = stringResource(R.string.session_completed_message, rememberPlantName(result.plant), result.coinsEarned),
            variant = pl.edu.pb.jardinito.ui.components.DialogVariant.Success,
            singleButton = true,
            confirmText = stringResource(R.string.ok)
        ),
        content = {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(100.dp)
                )
            }
        },
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}

@Composable
private fun SessionFailedDialog(
    result: SessionResult.Failed,
    onDismiss: () -> Unit
) {
    val imageUrl = rememberSvgImageRequest("${RetrofitInstance.BASE_URL}plants/${result.plant.witheredImages.medium}")
    ConfirmDialog(
        config = pl.edu.pb.jardinito.ui.components.DialogConfig(
            title = stringResource(R.string.session_failed_title),
            message = stringResource(R.string.session_failed_message, rememberPlantName(result.plant)),
            variant = pl.edu.pb.jardinito.ui.components.DialogVariant.Error,
            singleButton = true,
            confirmText = stringResource(R.string.ok)
        ),
        content = {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(100.dp)
                )
            }
        },
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}
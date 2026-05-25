package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import pl.edu.pb.jardinito.ui.components.CircularTimer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.ui.components.PlantPickerDrawer
import pl.edu.pb.jardinito.ui.components.TagPickerDrawer
import pl.edu.pb.jardinito.ui.components.appButton.AppButton
import pl.edu.pb.jardinito.ui.components.appButton.ButtonSize
import pl.edu.pb.jardinito.ui.components.appButton.ButtonVariant
import pl.edu.pb.jardinito.ui.theme.Dimensions
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.FocusViewModel
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
    val onStop: () -> Unit
)

data class TimerCallbacks(
    val onDurationChange: (Int) -> Unit,
    val onPlantClick: () -> Unit
)

data class TimerConfig(
    val devMode: Boolean = false,
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

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) tagViewModel.loadTags(userId)
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
            onStop = { focusViewModel.stop(userId) }
        ),
        config = config,
        onPlantSelected = { focusViewModel.selectPlant(it) },
        onTagSelected = { focusViewModel.selectTag(it) }
    )
}

@Composable
fun FocusScreenContent(
    timerUiState: TimerUiState,
    plantUiState: PlantUiState,
    tagUiState: TagUiState,
    timerActions: TimerActions,
    config: TimerConfig,
    onPlantSelected: (Plant) -> Unit,
    onTagSelected: (Tag?) -> Unit
) {
    val isIdle = timerUiState.timerState is TimerState.Idle
    val isRunning = timerUiState.timerState is TimerState.Running
    val isPaused = timerUiState.timerState is TimerState.Paused
    var showPlantPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }

    val minutes = timerUiState.remainingSeconds / 60
    val seconds = timerUiState.remainingSeconds % 60
    val timeText = "%d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary50)
            .padding(top = Dimensions.topBarHeight, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

        val unit = if (config.devMode) "s" else "min"

        Text(
            text = if (isIdle) "${timerUiState.selectedDuration} $unit" else timeText,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.neutralGray,
            modifier = Modifier.padding(top = 24.dp)
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
                showTagPicker = false
            },
            onDismiss = { showTagPicker = false }
        )
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
        modifier = Modifier.padding(top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            isIdle -> AppButton(
                text = "Start",
                size = ButtonSize.Large,
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
                    onClick = actions.onStop
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
                    onClick = actions.onStop
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
            .padding(top = 16.dp)
            .then(if (isIdle) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (selectedTag == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary100)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.pushpin),
                    contentDescription = null,
                    tint = colors.neutralGray,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.focus_add_tag),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.neutralGray
                )
            }
        } else {
            TagChip(tag = selectedTag)
        }
    }
}

@Composable
private fun TagChip(tag: Tag) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TagColors.colorCompose(tag.color).copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(TagColors.colorCompose(tag.color))
        )
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelMedium,
            color = TagColors.colorCompose(tag.color)
        )
    }
}
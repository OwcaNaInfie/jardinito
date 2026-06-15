package pl.edu.pb.jardinito.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.ui.components.AppChip
import pl.edu.pb.jardinito.ui.components.AppChipVariant
import pl.edu.pb.jardinito.ui.components.LoadingOverlay
import pl.edu.pb.jardinito.ui.screens.garden.GardenGrid
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.topBarHeight
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.viewmodel.GardenPeriod
import pl.edu.pb.jardinito.viewmodel.GardenViewModel

@Composable
fun GardenScreen(
    viewModel: GardenViewModel,
    userId: String
) {
    val sessions by viewModel.sessions.collectAsState()
    val period by viewModel.period.collectAsState()
    val isInitialized by viewModel.isInitialized.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val positions by viewModel.positions.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.load(userId)
        }
    }

    GardenScreenContent(
        sessions = sessions,
        period = period,
        isInitialized = isInitialized,
        isLoading = isLoading,
        positions = positions,
        gridSizeFor = viewModel::gridSizeFor,
        useSmallImage = viewModel::useSmallImage,
        onPeriodChange = { viewModel.setPeriod(it, userId) }
    )
}

@Composable
private fun GardenScreenContent(
    sessions: List<Session>,
    period: GardenPeriod,
    isInitialized: Boolean,
    isLoading: Boolean,
    positions: Map<String, Int>,
    gridSizeFor: (Int) -> Int,
    useSmallImage: (Int) -> Boolean,
    onPeriodChange: (GardenPeriod) -> Unit
) {
    val gridSize = gridSizeFor(sessions.size)
    val useSmall = useSmallImage(gridSize)

    Box(
    modifier = Modifier
        .fillMaxSize()
            .background(colors.primary100)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_sky),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillHeight
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topBarHeight, start = screenPadding_s, end = screenPadding_s),
            verticalArrangement = Arrangement.spacedBy(itemsSpacing_s)
        ) {
            PeriodFilterRow(
                selected = period,
                onSelect = onPeriodChange
            )

            if (!isInitialized || isLoading) {
                LoadingOverlay()
            } else if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.garden_empty),
                        style = typography.bodyMedium,
                        color = colors.neutralGray
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GardenGrid(
                        sessions = sessions,
                        gridSize = gridSize,
                        useSmall = useSmall,
                        positions = positions,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodFilterRow(
    selected: GardenPeriod,
    onSelect: (GardenPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing_xs, Alignment.End)
    ) {
        GardenPeriod.entries.forEach { period ->
            val label = when (period) {
                GardenPeriod.DAY   -> stringResource(R.string.garden_period_day)
                GardenPeriod.WEEK  -> stringResource(R.string.garden_period_week)
                GardenPeriod.MONTH -> stringResource(R.string.garden_period_month)
            }
            AppChip(
                isActive = period == selected,
                onClick = { onSelect(period) },
                variant = AppChipVariant.Outlined,
                text = label,
            )
        }
    }
}
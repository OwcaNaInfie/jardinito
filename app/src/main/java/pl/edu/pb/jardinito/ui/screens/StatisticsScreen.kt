package pl.edu.pb.jardinito.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import pl.edu.pb.jardinito.ui.utils.formatIdleTime
import pl.edu.pb.jardinito.viewmodel.GeneralStats
import pl.edu.pb.jardinito.viewmodel.SessionStatusStat
import androidx.compose.ui.platform.LocalContext
import pl.edu.pb.jardinito.ui.utils.PlantColor
import pl.edu.pb.jardinito.ui.utils.resolveString
import pl.edu.pb.jardinito.viewmodel.PlantStat
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.ui.components.AppChip
import pl.edu.pb.jardinito.ui.components.AppChipVariant
import pl.edu.pb.jardinito.ui.components.ChartEntry
import pl.edu.pb.jardinito.ui.components.ChipRow
import pl.edu.pb.jardinito.ui.components.ChipRowItem
import pl.edu.pb.jardinito.ui.components.LoadingOverlay
import pl.edu.pb.jardinito.ui.components.SessionsListItem
import pl.edu.pb.jardinito.ui.components.ToggleChart
import pl.edu.pb.jardinito.ui.screens.garden.GardenGrid
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.topBarHeight
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.rememberSvgImageRequest
import pl.edu.pb.jardinito.viewmodel.StatisticsPeriod
import pl.edu.pb.jardinito.viewmodel.StatisticsViewModel
import pl.edu.pb.jardinito.viewmodel.TagStat
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    userId: String,
    onSessionClick: (String) -> Unit,
) {
    val period by viewModel.period.collectAsState()
    val dateLabel by viewModel.dateLabel.collectAsState()
    val isAtCurrentPeriod by viewModel.isAtCurrentPeriod.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val positions by viewModel.positions.collectAsState()
    val generalStats by viewModel.generalStats.collectAsState()
    val tagStats by viewModel.tagStats.collectAsState()
    val plantStats by viewModel.plantStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isInitialized by viewModel.isInitialized.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.load(userId)
        }
    }

    StatisticsScreenContent(
        period = period,
        dateLabel = dateLabel,
        isAtCurrentPeriod = isAtCurrentPeriod,
        sessions = sessions,
        positions = positions,
        generalStats = generalStats,
        tagStats = tagStats,
        plantStats = plantStats,
        isLoading = isLoading,
        isInitialized = isInitialized,
        gridSizeFor = viewModel::gridSizeFor,
        useSmallImage = viewModel::useSmallImage,
        onPeriodChange = viewModel::setPeriod,
        onPreviousClick = viewModel::navigatePrevious,
        onNextClick = viewModel::navigateNext,
        onResetToToday = viewModel::resetToToday,
        onSessionClick = onSessionClick
    )
}

@Composable
private fun StatisticsScreenContent(
    period: StatisticsPeriod,
    dateLabel: String,
    isAtCurrentPeriod: Boolean,
    sessions: List<Session>,
    positions: Map<String, Int>,
    generalStats: GeneralStats,
    tagStats: List<TagStat>,
    plantStats: List<PlantStat>,
    isLoading: Boolean,
    isInitialized: Boolean,
    gridSizeFor: (Int) -> Int,
    useSmallImage: (Int) -> Boolean,
    onPeriodChange: (StatisticsPeriod) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onResetToToday: () -> Unit,
    onSessionClick: (String) -> Unit
) {
    val gridSize = gridSizeFor(sessions.size)
    val useSmall = useSmallImage(gridSize)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.statistics_tab_general),
        stringResource(R.string.statistics_tab_garden),
        stringResource(R.string.statistics_tab_tags)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutralLight)
            .padding(top = topBarHeight, start = screenPadding_s, end = screenPadding_s),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s)
    ) {
        PeriodSelector(selected = period, onSelect = onPeriodChange)
        DateNavigator(
            label = dateLabel,
            isAtCurrentPeriod = isAtCurrentPeriod,
            onPrevious = onPreviousClick,
            onNext = onNextClick,
            onResetToToday = onResetToToday
        )
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = colors.neutralLight,
            contentColor = colors.primary500
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = typography.labelMedium) }
                )
            }
        }

        if (!isInitialized || isLoading) {
            LoadingOverlay()
        } else if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.garden_empty),
                    style = typography.bodyMedium,
                    color = colors.neutralGray
                )
            }
        } else {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    0 -> GeneralTabContent(generalStats = generalStats)
                    1 -> GardenTabContent(
                        sessions = sessions,
                        positions = positions,
                        gridSize = gridSize,
                        useSmall = useSmall,
                        onSessionClick = onSessionClick,
                        plantStats = plantStats
                    )
                    2 -> TagsTabContent(tagStats = tagStats)
                }
            }
        }
    }
}

@Composable
private fun GeneralTabContent(generalStats: GeneralStats) {
    val focusChartEntries = generalStats.focusTimeEntries.map { entry ->
        ChartEntry(label = entry.label, value = entry.value.toFloat(), color = colors.primary500)
    }
    val statusChartEntries = generalStats.statusStats.map { stat ->
        ChartEntry(
            label = if (stat.status == "completed") stringResource(R.string.statistics_status_completed)
            else stringResource(R.string.statistics_status_uncompleted),
            value = stat.count.toFloat(),
            color = if (stat.status == "completed") colors.primary500 else colors.error
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
        contentPadding = PaddingValues(bottom = itemsSpacing_s)
    ) {
        item {
            StatInfoRow(
                label = stringResource(R.string.statistics_total_focus),
                value = formatIdleTime(generalStats.totalFocusTime, devMode = true)
            )
        }
        generalStats.bestDay?.let { day ->
            item {
                StatInfoRow(
                    label = stringResource(R.string.statistics_best_day),
                    value = day
                )
            }
        }
        if (focusChartEntries.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.statistics_focus_time_title)) }
            item { ToggleChart(entries = focusChartEntries, modifier = Modifier.fillMaxWidth()) }
        }
        if (statusChartEntries.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.statistics_status_title)) }
            item { ToggleChart(entries = statusChartEntries, modifier = Modifier.fillMaxWidth()) }
            item {
                Column {
                    generalStats.statusStats.forEachIndexed { index, stat ->
                        StatusStatRow(stat = stat)
                        if (index < generalStats.statusStats.lastIndex) {
                            HorizontalDivider(color = colors.neutralInvisibleGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = typography.titleSmall, color = colors.neutralGray)
}

@Composable
private fun StatInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = typography.bodyMedium, color = colors.neutralLightGray)
        Text(text = value, style = typography.bodyMedium, color = colors.neutralGray)
    }
}

@Composable
private fun StatusStatRow(stat: SessionStatusStat) {
    val color = if (stat.status == "completed") colors.primary500 else colors.error
    val label = if (stat.status == "completed") stringResource(R.string.statistics_status_completed)
    else stringResource(R.string.statistics_status_uncompleted)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = itemsSpacing_xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing_s)
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Text(text = label, style = typography.bodyMedium, color = colors.neutralGray, modifier = Modifier.weight(1f))
        Text(text = "${stat.count}x", style = typography.bodyMedium, color = colors.neutralGray)
        Text(text = "${stat.percentage.roundToInt()}%", style = typography.bodyMedium, color = colors.neutralGray)
    }
}

@Composable
private fun GardenTabContent(
    sessions: List<Session>,
    positions: Map<String, Int>,
    gridSize: Int,
    useSmall: Boolean,
    plantStats: List<PlantStat>,
    onSessionClick: (String) -> Unit
) {
    val context = LocalContext.current
    val chartEntries = plantStats.map { stat ->
        ChartEntry(
            label = resolveString(context, stat.plant.nameKey),
            value = stat.count.toFloat(),
            color = PlantColor.fromKey(stat.plant.colors.getOrElse(1) {
                stat.plant.colors.firstOrNull() ?: "" })?.color
                ?: colors.neutralGray
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
        contentPadding = PaddingValues(bottom = itemsSpacing_s)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GardenGrid(
                    sessions = sessions,
                    gridSize = gridSize,
                    useSmall = useSmall,
                    positions = positions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            ToggleChart(
                entries = chartEntries,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Column {
                plantStats.forEachIndexed { index, stat ->
                    PlantStatRow(stat = stat)
                    if (index < plantStats.lastIndex) {
                        HorizontalDivider(color = colors.neutralInvisibleGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
        item {
            CollapsibleSessionList(sessions = sessions, onSessionClick = onSessionClick)
        }
    }
}

@Composable
private fun PlantStatRow(stat: PlantStat) {
    val context = LocalContext.current
    val imageUrl = "${RetrofitInstance.BASE_URL}plants/${stat.plant.images.large}"
    val request = rememberSvgImageRequest(imageUrl)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = itemsSpacing_xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = request,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None
            )
            Text(
                text = resolveString(context, stat.plant.nameKey),
                style = typography.bodyMedium,
                color = colors.neutralGray
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(itemsSpacing_xs)) {
            Text(
                text = "${stat.count}x",
                style = typography.bodyMedium,
                color = colors.neutralGray
            )
            Text(
                text = "${stat.percentage.roundToInt()}%",
                style = typography.bodyMedium,
                color = colors.neutralGray
            )
        }
    }
}

@Composable
private fun TagsTabContent(tagStats: List<TagStat>) {
    val chartEntries = tagStats.map { stat ->
        ChartEntry(
            label = stat.tag?.name ?: "-",
            value = stat.count.toFloat(),
            color = stat.tag?.let { TagColors.colorCompose(it.color) } ?: colors.neutralGray
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
        contentPadding = PaddingValues(bottom = itemsSpacing_s)
    ) {
        item {
            ToggleChart(entries = chartEntries, modifier = Modifier.fillMaxWidth())
        }
        items(tagStats) { stat ->
            TagStatRow(stat = stat)
        }
    }
}

@Composable
private fun TagStatRow(stat: TagStat) {
    val tagColor = stat.tag?.let { TagColors.colorCompose(it.color) } ?: colors.neutralGray

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing_s)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(tagColor)
        )
        Text(
            text = stat.tag?.name ?: stringResource(R.string.session_detail_no_tag),
            style = typography.bodyMedium,
            color = colors.neutralGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${stat.count}x",
            style = typography.bodyMedium,
            color = colors.neutralGray
        )
        Text(
            text = "${stat.percentage.roundToInt()}%",
            style = typography.bodyMedium,
            color = colors.neutralGray
        )
    }
}

@Composable
private fun CollapsibleSessionList(
    sessions: List<Session>,
    onSessionClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = itemsSpacing_xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.statistics_sessions_header, sessions.size),
                style = typography.titleSmall,
                color = colors.neutralGray
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = colors.neutralGray
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier.padding(top = itemsSpacing_xs),
                verticalArrangement = Arrangement.spacedBy(itemsSpacing_xs)
            ) {
                sessions.forEach { session ->
                    SessionsListItem(session = session, onClick = { onSessionClick(session.sessionId) })
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: StatisticsPeriod, onSelect: (StatisticsPeriod) -> Unit) {
    ChipRow(
        items = StatisticsPeriod.entries.map { period ->
            ChipRowItem(
                text = stringResource(period.labelRes),
                isActive = period == selected,
                onClick = { onSelect(period) }
            )
        },
    )
}

@Composable
private fun DateNavigator(
    label: String,
    isAtCurrentPeriod: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onResetToToday: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.neutralGray)
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = typography.bodyMedium, color = colors.neutralGray, textAlign = TextAlign.Center)
            if (!isAtCurrentPeriod) {
                IconButton(onClick = onResetToToday, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Replay, contentDescription = null, tint = colors.neutralGray, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (!isAtCurrentPeriod) {
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colors.neutralGray)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}
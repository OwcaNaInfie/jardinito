package pl.edu.pb.jardinito.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.ui.components.ChipRow
import pl.edu.pb.jardinito.ui.components.ChipRowItem
import pl.edu.pb.jardinito.ui.components.LoadingOverlay
import pl.edu.pb.jardinito.ui.components.SessionsListItem
import pl.edu.pb.jardinito.ui.components.charts.ChartEntry
import pl.edu.pb.jardinito.ui.components.charts.ChartLegendItem
import pl.edu.pb.jardinito.ui.components.charts.ChartSection
import pl.edu.pb.jardinito.ui.components.charts.ChartType
import pl.edu.pb.jardinito.ui.screens.garden.GardenGrid
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.itemsSpacing_xs
import pl.edu.pb.jardinito.ui.theme.Dimensions.screenPadding_s
import pl.edu.pb.jardinito.ui.theme.Dimensions.topBarHeight
import pl.edu.pb.jardinito.ui.theme.TagColors
import pl.edu.pb.jardinito.ui.theme.colors
import pl.edu.pb.jardinito.ui.utils.PlantColor
import pl.edu.pb.jardinito.ui.utils.formatIdleTime
import pl.edu.pb.jardinito.ui.utils.resolveString
import pl.edu.pb.jardinito.viewmodel.GeneralStats
import pl.edu.pb.jardinito.viewmodel.PlantStat
import pl.edu.pb.jardinito.viewmodel.StatisticsPeriod
import pl.edu.pb.jardinito.viewmodel.StatisticsViewModel
import pl.edu.pb.jardinito.viewmodel.TagStat

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
    onSessionClick: (String) -> Unit,
) {
    val gridSize = gridSizeFor(sessions.size)
    val useSmall = useSmallImage(gridSize)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var chartType by rememberSaveable { mutableStateOf(ChartType.Bar) }

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
                    text = { Text(title, style = typography.labelSmall) }
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
                    0 -> GeneralTabContent(
                        generalStats = generalStats,
                        period = period,
                        chartType = chartType,
                        onChartTypeChange = { chartType = it }
                    )
                    1 -> GardenTabContent(
                        sessions = sessions,
                        positions = positions,
                        gridSize = gridSize,
                        useSmall = useSmall,
                        plantStats = plantStats,
                        chartType = chartType,
                        onChartTypeChange = { chartType = it },
                        onSessionClick = onSessionClick
                    )
                    2 -> TagsTabContent(
                        tagStats = tagStats,
                        chartType = chartType,
                        onChartTypeChange = { chartType = it }
                    )
                }
            }
        }
    }
}

// =====================
// TABS
// =====================

@Composable
private fun GeneralTabContent(
    generalStats: GeneralStats,
    period: StatisticsPeriod,
    chartType: ChartType,
    onChartTypeChange: (ChartType) -> Unit
) {
    val focusEntries = generalStats.focusTimeEntries.map { entry ->
        ChartEntry(label = entry.label, legendLabel = entry.legendLabel, value = entry.value.toFloat(), color = colors.primary500)
    }

    val hourAbbr = stringResource(R.string.time_hour_abbr)

    val focusTotal = generalStats.focusTimeEntries.sumOf { it.value }.toFloat()
        .takeIf { it > 0f } ?: 1f
    val focusLegend = generalStats.focusTimeEntries
        .filter { it.value > 0 }
        .map { entry ->
            ChartLegendItem(
                label = entry.legendLabel,
                count = entry.value,
                percentage = entry.value / focusTotal * 100f,
                color = colors.primary500
            )
        }

    val statusEntries = generalStats.statusStats.map { stat ->
        ChartEntry(
            label = if (stat.status == "completed") stringResource(R.string.statistics_status_completed)
            else stringResource(R.string.statistics_status_uncompleted),
            value = stat.count.toFloat(),
            color = if (stat.status == "completed") colors.primary500 else colors.error
        )
    }
    val statusLegend = generalStats.statusStats.map { stat ->
        ChartLegendItem(
            label = if (stat.status == "completed") stringResource(R.string.statistics_status_completed)
            else stringResource(R.string.statistics_status_uncompleted),
            count = stat.count,
            percentage = stat.percentage,
            color = if (stat.status == "completed") colors.primary500 else colors.error
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
        contentPadding = PaddingValues(vertical = itemsSpacing_s)
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
        if (focusEntries.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.statistics_focus_time_title)) }
            item {
                ChartSection(
                    entries = focusEntries,
                    legendItems = focusLegend,
                    legendValueFormatter = { count ->
                        val hours = count / 60
                        val minutes = count % 60
                        when {
                            hours > 0 && minutes > 0 -> "$hours $hourAbbr $minutes min"
                            hours > 0                 -> "$hours $hourAbbr"
                            else                      -> "$minutes min"
                        }
                    },
                    chartType = chartType,
                    onChartTypeChange = onChartTypeChange
                )
            }
        }
        if (statusEntries.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.statistics_status_title)) }
            item {
                ChartSection(
                    entries = statusEntries,
                    legendItems = statusLegend,
                    chartType = chartType,
                    onChartTypeChange = onChartTypeChange
                )
            }
        }
    }
}

@Composable
private fun GardenTabContent(
    sessions: List<Session>,
    positions: Map<String, Int>,
    gridSize: Int,
    useSmall: Boolean,
    plantStats: List<PlantStat>,
    chartType: ChartType,
    onChartTypeChange: (ChartType) -> Unit,
    onSessionClick: (String) -> Unit
) {
    val context = LocalContext.current

    val plantEntries = plantStats.map { stat ->
        ChartEntry(
            label = resolveString(context, stat.plant.nameKey),
            value = stat.count.toFloat(),
            color = PlantColor.fromKey(
                stat.plant.colors.getOrElse(1) { stat.plant.colors.firstOrNull() ?: "" }
            )?.color ?: colors.neutralGray
        )
    }
    val plantLegend = plantStats.map { stat ->
        ChartLegendItem(
            label = resolveString(context, stat.plant.nameKey),
            count = stat.count,
            percentage = stat.percentage,
            color = PlantColor.fromKey(
                stat.plant.colors.getOrElse(1) { stat.plant.colors.firstOrNull() ?: "" }
            )?.color ?: colors.neutralGray
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
        contentPadding = PaddingValues(vertical = itemsSpacing_s)
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
            ChartSection(
                entries = plantEntries,
                legendItems = plantLegend,
                chartType = chartType,
                onChartTypeChange = onChartTypeChange
            )
        }
        item {
            CollapsibleSessionList(sessions = sessions, onSessionClick = onSessionClick)
        }
    }
}

@Composable
private fun TagsTabContent(
    tagStats: List<TagStat>,
    chartType: ChartType,
    onChartTypeChange: (ChartType) -> Unit
) {
    val tagEntries = tagStats.map { stat ->
        ChartEntry(
            label = stat.tag?.name ?: "-",
            value = stat.count.toFloat(),
            color = stat.tag?.let { TagColors.colorCompose(it.color) } ?: colors.neutralGray
        )
    }
    val tagLegend = tagStats.map { stat ->
        ChartLegendItem(
            label = stat.tag?.name ?: stringResource(R.string.session_detail_no_tag),
            count = stat.count,
            percentage = stat.percentage,
            color = stat.tag?.let { TagColors.colorCompose(it.color) } ?: colors.neutralGray
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing_s),
        contentPadding = PaddingValues(vertical = itemsSpacing_s)
    ) {
        item {
            ChartSection(
                entries = tagEntries,
                legendItems = tagLegend,
                chartType = chartType,
                onChartTypeChange = onChartTypeChange
            )
        }
    }
}

// =====================
// HELPERS
// =====================

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
                    SessionsListItem(
                        session = session,
                        onClick = { onSessionClick(session.sessionId) }
                    )
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
        }
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
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = colors.neutralGray
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = typography.bodyMedium,
                color = colors.neutralGray,
                textAlign = TextAlign.Center
            )
            if (!isAtCurrentPeriod) {
                IconButton(onClick = onResetToToday, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Replay,
                        contentDescription = null,
                        tint = colors.neutralGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        if (!isAtCurrentPeriod) {
            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = colors.neutralGray
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}
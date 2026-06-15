package pl.edu.pb.jardinito.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.edu.pb.jardinito.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.repository.SessionRepository
import java.text.SimpleDateFormat
import kotlinx.coroutines.flow.map
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.model.Tag
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.sqrt

enum class StatisticsPeriod(@StringRes val labelRes: Int) {
    DAY(R.string.statistics_period_day),
    WEEK(R.string.statistics_period_week),
    MONTH(R.string.statistics_period_month),
    YEAR(R.string.statistics_period_year)
}

data class TagStat(
    val tag: Tag?,
    val count: Int,
    val percentage: Float
)

data class PlantStat(
    val plant: Plant,
    val count: Int,
    val percentage: Float
)

data class SessionStatusStat(
    val status: String,
    val count: Int,
    val percentage: Float
)

data class FocusTimeEntry(
    val label: String,
    val value: Int
)

data class GeneralStats(
    val totalFocusTime: Int,
    val bestDay: String?,
    val statusStats: List<SessionStatusStat>,
    val focusTimeEntries: List<FocusTimeEntry>
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _period = MutableStateFlow(StatisticsPeriod.DAY)
    val period: StateFlow<StatisticsPeriod> = _period

    private val _selectedDateMs = MutableStateFlow(todayMs())

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions

    private val _positions = MutableStateFlow<Map<String, Int>>(emptyMap())
    val positions: StateFlow<Map<String, Int>> = _positions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private var currentUserId: String = ""

    val dateLabel: StateFlow<String> = combine(_period, _selectedDateMs) { period, ms ->
        formatDate(calendarOf(ms), period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isAtCurrentPeriod: StateFlow<Boolean> = combine(_period, _selectedDateMs) { period, ms ->
        isCurrentPeriod(calendarOf(ms), period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val tagStats: StateFlow<List<TagStat>> = _sessions
        .map { sessions ->
            if (sessions.isEmpty()) return@map emptyList()
            sessions
                .groupBy { it.tag?.tagId }
                .map { (_, group) ->
                    TagStat(
                        tag = group.first().tag,
                        count = group.size,
                        percentage = group.size.toFloat() / sessions.size * 100f
                    )
                }
                .sortedByDescending { it.count }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plantStats: StateFlow<List<PlantStat>> = _sessions
        .map { sessions ->
            if (sessions.isEmpty()) return@map emptyList()
            sessions.groupBy { it.plant.plantId }
                .map { (_, group) ->
                    PlantStat(
                        plant = group.first().plant,
                        count = group.size,
                        percentage = group.size.toFloat() / sessions.size * 100f
                    )
                }
                .sortedByDescending { it.count }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    val generalStats: StateFlow<GeneralStats> = combine(_sessions, _period, _selectedDateMs) { sessions, period, dateMs ->
        computeGeneralStats(sessions, period, dateMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GeneralStats(0, null, emptyList(), emptyList()))

    fun load(userId: String) {
        currentUserId = userId
        fetchSessions()
    }

    fun setPeriod(period: StatisticsPeriod) {
        _period.value = period
        _selectedDateMs.value = todayMs()
        fetchSessions()
    }

    fun navigatePrevious() {
        _selectedDateMs.update { navigate(calendarOf(it), _period.value, -1).timeInMillis }
        fetchSessions()
    }

    fun navigateNext() {
        _selectedDateMs.update { navigate(calendarOf(it), _period.value, +1).timeInMillis }
        fetchSessions()
    }

    fun resetToToday() {
        _selectedDateMs.value = todayMs()
        fetchSessions()
    }

    fun gridSizeFor(sessionCount: Int): Int {
        if (sessionCount == 0) return 3
        return ceil(sqrt(sessionCount.toDouble())).toInt().coerceAtLeast(3)
    }

    fun useSmallImage(gridSize: Int): Boolean = gridSize > 5

    private fun fetchSessions() {
        if (currentUserId.isEmpty()) return
        val (from, to) = dateRange(calendarOf(_selectedDateMs.value), _period.value)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = sessionRepository.getSessionsByDateRange(
                    userId = currentUserId,
                    from = toIsoString(from),
                    to = toIsoString(to)
                )
                _sessions.value = result
                _positions.value = result.mapIndexed { i, s -> s.sessionId to i }.toMap()
            } catch (e: Exception) {
                _sessions.value = emptyList()
            } finally {
                _isLoading.value = false
                _isInitialized.value = true
            }
        }
    }
}

// =====================
// HELPERS
// =====================

private fun todayMs(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun calendarOf(ms: Long): Calendar =
    Calendar.getInstance().apply { timeInMillis = ms }

private fun navigate(date: Calendar, period: StatisticsPeriod, direction: Int): Calendar =
    (date.clone() as Calendar).apply {
        when (period) {
            StatisticsPeriod.DAY   -> add(Calendar.DAY_OF_MONTH, direction)
            StatisticsPeriod.WEEK  -> add(Calendar.WEEK_OF_YEAR, direction)
            StatisticsPeriod.MONTH -> add(Calendar.MONTH, direction)
            StatisticsPeriod.YEAR  -> add(Calendar.YEAR, direction)
        }
    }

private fun dateRange(date: Calendar, period: StatisticsPeriod): Pair<Calendar, Calendar> =
    when (period) {
        StatisticsPeriod.DAY -> Pair(startOfDay(date), endOfDay(date))
        StatisticsPeriod.WEEK -> {
            val from = startOfDay(weekStart(date))
            val to   = endOfDay((from.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) })
            Pair(from, to)
        }
        StatisticsPeriod.MONTH -> {
            val from = startOfDay((date.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) })
            val to   = endOfDay((date.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            })
            Pair(from, to)
        }
        StatisticsPeriod.YEAR -> {
            val from = startOfDay((date.clone() as Calendar).apply {
                set(Calendar.MONTH, Calendar.JANUARY); set(Calendar.DAY_OF_MONTH, 1)
            })
            val to   = endOfDay((date.clone() as Calendar).apply {
                set(Calendar.MONTH, Calendar.DECEMBER); set(Calendar.DAY_OF_MONTH, 31)
            })
            Pair(from, to)
        }
    }

private fun startOfDay(date: Calendar) = (date.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
}

private fun endOfDay(date: Calendar) = (date.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59);      set(Calendar.MILLISECOND, 999)
}

private fun weekStart(date: Calendar): Calendar =
    (date.clone() as Calendar).apply {
        val daysSinceMonday = (get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        add(Calendar.DAY_OF_MONTH, -daysSinceMonday)
    }

private fun isCurrentPeriod(date: Calendar, period: StatisticsPeriod): Boolean {
    val today = calendarOf(todayMs())
    return when (period) {
        StatisticsPeriod.DAY   -> sameDay(date, today)
        StatisticsPeriod.WEEK  -> sameDay(weekStart(date), weekStart(today))
        StatisticsPeriod.MONTH -> date.get(Calendar.YEAR)  == today.get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
        StatisticsPeriod.YEAR  -> date.get(Calendar.YEAR)  == today.get(Calendar.YEAR)
    }
}

private fun sameDay(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun toIsoString(cal: Calendar): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(cal.time)

private fun formatDate(date: Calendar, period: StatisticsPeriod): String = when (period) {
    StatisticsPeriod.DAY ->
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date.time)
    StatisticsPeriod.WEEK -> {
        val start = weekStart(date)
        val end   = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }
        if (start.get(Calendar.MONTH) == end.get(Calendar.MONTH)) {
            "${start.get(Calendar.DAY_OF_MONTH)}–${SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(end.time)}"
        } else {
            "${SimpleDateFormat("d MMM", Locale.getDefault()).format(start.time)} – ${SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(end.time)}"
        }
    }
    StatisticsPeriod.MONTH ->
        SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(date.time).replaceFirstChar { it.uppercase() }
    StatisticsPeriod.YEAR ->
        SimpleDateFormat("yyyy", Locale.getDefault()).format(date.time)
}
private fun computeGeneralStats(sessions: List<Session>, period: StatisticsPeriod, dateMs: Long): GeneralStats {
    val completed = sessions.filter { it.status == "completed" }
    val uncompleted = sessions.filter { it.status != "completed" }
    val total = sessions.size

    val statusStats = if (total == 0) emptyList() else listOf(
        SessionStatusStat("completed", completed.size, completed.size.toFloat() / total * 100f),
        SessionStatusStat("uncompleted", uncompleted.size, uncompleted.size.toFloat() / total * 100f)
    )

    return GeneralStats(
        totalFocusTime = completed.sumOf { it.actualDuration ?: 0 },
        bestDay = if (period == StatisticsPeriod.DAY) null else computeBestDay(completed),
        statusStats = statusStats,
        focusTimeEntries = computeFocusTimeEntries(completed, period, dateMs)
    )
}

private fun computeBestDay(completed: List<Session>): String? {
    if (completed.isEmpty()) return null
    val best = completed
        .groupBy { it.startedAt.take(10) }
        .maxByOrNull { (_, group) -> group.sumOf { it.actualDuration ?: 0 } }
        ?.key ?: return null
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(best) ?: return best
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date)
    } catch (e: Exception) { best }
}

private fun computeFocusTimeEntries(
    completed: List<Session>,
    period: StatisticsPeriod,
    dateMs: Long
): List<FocusTimeEntry> {
    val date = calendarOf(dateMs)
    return when (period) {
        StatisticsPeriod.DAY -> completed
            .sortedBy { it.startedAt }
            .map { FocusTimeEntry(it.startedAt.substring(11, 16), it.actualDuration ?: 0) }

        StatisticsPeriod.WEEK -> {
            val monday = weekStart(date)
            (0..6).map { offset ->
                val day = (monday.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
                FocusTimeEntry(
                    label = SimpleDateFormat("EEE", Locale.getDefault()).format(day.time).take(2),
                    value = completed.filter { sessionOnDay(it, day) }.sumOf { it.actualDuration ?: 0 }
                )
            }
        }

        StatisticsPeriod.MONTH -> {
            val firstDay = (date.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            (0 until date.getActualMaximum(Calendar.DAY_OF_MONTH)).map { offset ->
                val day = offset + 1
                val dayCalendar = (firstDay.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
                FocusTimeEntry(
                    label = if (day == 1 || day % 5 == 0) day.toString() else "",
                    value = completed.filter { sessionOnDay(it, dayCalendar) }.sumOf { it.actualDuration ?: 0 }
                )
            }
        }

        StatisticsPeriod.YEAR -> (0..11).map { monthIndex ->
            val month = (date.clone() as Calendar).apply {
                set(Calendar.MONTH, monthIndex); set(Calendar.DAY_OF_MONTH, 1)
            }
            FocusTimeEntry(
                label = SimpleDateFormat("MMM", Locale.getDefault()).format(month.time).take(3),
                value = completed.filter { sessionOnMonth(it, month) }.sumOf { it.actualDuration ?: 0 }
            )
        }
    }
}

private fun sessionOnDay(session: Session, day: Calendar): Boolean {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        val parsed = fmt.parse(session.startedAt.take(19)) ?: return false
        val cal = Calendar.getInstance().apply { time = parsed }
        cal.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
    } catch (e: Exception) { false }
}

private fun sessionOnMonth(session: Session, month: Calendar): Boolean {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        val parsed = fmt.parse(session.startedAt.take(19)) ?: return false
        val cal = Calendar.getInstance().apply { time = parsed }
        cal.get(Calendar.YEAR) == month.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == month.get(Calendar.MONTH)
    } catch (e: Exception) { false }
}
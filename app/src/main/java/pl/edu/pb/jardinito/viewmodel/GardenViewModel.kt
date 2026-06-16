package pl.edu.pb.jardinito.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.R
import pl.edu.pb.jardinito.data.manager.GardenPositionsManager
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.repository.PlantRepository
import pl.edu.pb.jardinito.data.repository.SessionRepository
import javax.inject.Inject

enum class GardenPeriod(val apiValue: String, @StringRes val labelRes: Int) {
    DAY("day",   R.string.garden_period_day),
    WEEK("week", R.string.garden_period_week),
    MONTH("month", R.string.garden_period_month)
}

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val gardenPositionsManager: GardenPositionsManager,
    private val plantRepository: PlantRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions

    private val _period = MutableStateFlow(GardenPeriod.DAY)
    val period: StateFlow<GardenPeriod> = _period

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _positions = MutableStateFlow<Map<String, Int>>(emptyMap())
    val positions: StateFlow<Map<String, Int>> = _positions

    fun load(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                plantRepository.getPlants()
                val result = sessionRepository.getSessionsByPreset(
                    userId = userId,
                    period = _period.value.apiValue
                )
                android.util.Log.d("GardenVM", "sessions: ${result.size}, plants: ${result.map { it.plant.plantId }}")
                _sessions.value = result
                assignPositions(result)
                android.util.Log.d("GardenVM", "positions: ${_positions.value}")
            } catch (e: Exception) {
                android.util.Log.e("GardenVM", "load failed: ${e::class.simpleName} ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
                _isInitialized.value = true
            }
        }
    }

    fun updateSessionTag(sessionId: String, tag: Tag?) {
        _sessions.update { list ->
            list.map { if (it.sessionId == sessionId) it.copy(tag = tag) else it }
        }
        viewModelScope.launch {
            try {
                sessionRepository.updateSessionTag(sessionId, tag)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun setPeriod(period: GardenPeriod, userId: String) {
        _period.update { period }
        load(userId)
    }

    fun gridSizeFor(sessionCount: Int): Int {
        if (sessionCount == 0) return 3  // minimalny rozmiar dla pustego stanu
        return ceil(sqrt(sessionCount.toDouble())).toInt().coerceAtLeast(3)
    }

    fun useSmallImage(gridSize: Int): Boolean = gridSize > 5

    private fun assignPositions(sessions: List<Session>) {
        if (sessions.isEmpty()) {
            _positions.value = emptyMap()
            return
        }

        val gridSize = gridSizeFor(sessions.size)
        val totalCells = gridSize * gridSize

        val saved = gardenPositionsManager.getPositions(gridSize)
            ?.toMutableMap() ?: mutableMapOf()

        // Usuń zapisane pozycje które wychodzą poza aktualny grid
        saved.entries.removeIf { it.value >= totalCells }

        val unassigned = sessions.filter { it.sessionId !in saved }

        if (unassigned.isEmpty()) {
            _positions.value = saved
            return
        }

        val takenPositions = saved.values.toSet()
        val freePositions = (0 until totalCells)
            .filter { it !in takenPositions }
            .shuffled()

        // Zabezpieczenie gdyby freePositions było krótsze niż unassigned
        unassigned.forEachIndexed { index, session ->
            if (index < freePositions.size) {
                saved[session.sessionId] = freePositions[index]
            }
        }

        gardenPositionsManager.savePositions(gridSize, saved)
        _positions.value = saved
    }
}
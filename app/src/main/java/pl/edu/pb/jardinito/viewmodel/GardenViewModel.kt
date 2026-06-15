package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.manager.GardenPositionsManager
import pl.edu.pb.jardinito.data.model.Session
import pl.edu.pb.jardinito.data.repository.SessionRepository
import javax.inject.Inject

enum class GardenPeriod(val apiValue: String) {
    DAY("day"),
    WEEK("week"),
    MONTH("month")
}

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val gardenPositionsManager: GardenPositionsManager
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
                val result = sessionRepository.getSessionsByPreset(
                    userId = userId,
                    period = _period.value.apiValue
                )
                _sessions.value = result
                assignPositions(result)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
                _isInitialized.value = true
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

        // Wczytaj zapisane pozycje dla tego rozmiaru grida
        val saved = gardenPositionsManager.getPositions(gridSize)
            ?.toMutableMap() ?: mutableMapOf()

        // Znajdź sesje bez przypisanej pozycji
        val unassigned = sessions.filter { it.sessionId !in saved }

        if (unassigned.isEmpty()) {
            _positions.value = saved
            return
        }

        // Wylosuj wolne pozycje tylko dla nowych sesji
        val takenPositions = saved.values.toSet()
        val freePositions = (0 until totalCells)
            .filter { it !in takenPositions }
            .shuffled()

        unassigned.forEachIndexed { index, session ->
            saved[session.sessionId] = freePositions[index]
        }

        gardenPositionsManager.savePositions(gridSize, saved)
        _positions.value = saved
    }
}
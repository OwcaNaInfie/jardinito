package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.repository.PlantRepository
import pl.edu.pb.jardinito.data.repository.SessionRepository
import pl.edu.pb.jardinito.data.repository.WalletRepository
import pl.edu.pb.jardinito.viewmodel.state.TimerState
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val sessionRepository: SessionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    // =====================
    // STATE
    // =====================

    val devMode = false

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState

    private val _selectedDuration = MutableStateFlow(5)
    val selectedDuration: StateFlow<Int> = _selectedDuration

    // totalSeconds() może być wywołane po inicjalizacji _selectedDuration
    private val _remainingSeconds = MutableStateFlow(totalSeconds())
    val remainingSeconds: StateFlow<Int> = _remainingSeconds

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    private val _selectedPlant = MutableStateFlow<Plant?>(null)
    val selectedPlant: StateFlow<Plant?> = _selectedPlant

    private val _selectedTag = MutableStateFlow<Tag?>(null)
    val selectedTag: StateFlow<Tag?> = _selectedTag

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins

    private val _lastEarnedCoins = MutableStateFlow(0)
    val lastEarnedCoins: StateFlow<Int> = _lastEarnedCoins

    private var timerJob: Job? = null
    private var sessionStartedAt: String? = null

    // =====================
    // INIT
    // =====================

    init {
        loadPlants()
    }

    // =====================
    // ACTIONS
    // =====================

    private fun loadPlants() {
        viewModelScope.launch {
            try {
                val result = plantRepository.getPlants()
                _plants.value = result

                // Zawsze wybierz tulipan (price == 0), fallback na pierwszą roślinę
                val defaultPlant = result.firstOrNull { it.price == 0 } ?: result.firstOrNull()
                defaultPlant?.let { plant ->
                    _selectedPlant.value = plant
                    // Ustaw timer na minimum wybranej rośliny
                    updateDuration(if (devMode) plant.minDurationDev else plant.minDuration)
                }
            } catch (e: Exception) { }
        }
    }

    fun loadCoins(userId: String) {
        viewModelScope.launch {
            try {
                _coins.value = walletRepository.getCoins(userId)
            } catch (e: Exception) { }
        }
    }

    fun selectPlant(plant: Plant) {
        if (_timerState.value !is TimerState.Idle) return
        _selectedPlant.value = plant
        updateDuration(if (devMode) plant.minDurationDev else plant.minDuration)
    }

    fun selectTag(tag: Tag?) {
        _selectedTag.value = tag
    }

    fun setDurationDev(seconds: Int) = updateDuration(seconds)
    fun setDuration(minutes: Int) = updateDuration(minutes)

    fun start(userId: String) {
        if (_selectedPlant.value == null) return
        sessionStartedAt = nowIso()
        _timerState.value = TimerState.Running
        runTimer(userId)
    }

    fun pause() {
        timerJob?.cancel()
        _timerState.value = TimerState.Paused
    }

    fun resume(userId: String) {
        _timerState.value = TimerState.Running
        runTimer(userId)
    }

    fun stop(userId: String) {
        timerJob?.cancel()
        val plant = _selectedPlant.value
        if (plant != null && sessionStartedAt != null) {
            viewModelScope.launch {
                saveSession(userId, plant, status = "failed")
            }
        }
        resetToIdle()
        sessionStartedAt = null
    }

    // =====================
    // PRIVATE HELPERS
    // =====================

    // Shared timer loop — used by start() and resume()
    private fun runTimer(userId: String) {
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                _progress.value = _remainingSeconds.value.toFloat() / totalSeconds().toFloat()
            }
            delay(1000)
            val plant = _selectedPlant.value ?: return@launch
            saveSession(userId, plant, status = "completed")
            resetToIdle()
        }
    }

    private suspend fun saveSession(userId: String, plant: Plant, status: String) {
        try {
            val now = nowIso()
            // Elapsed time in minutes — works for both dev (seconds/60) and prod (seconds/60)
            val actualDuration = (totalSeconds() - _remainingSeconds.value) / 60

            val response = sessionRepository.createSession(
                userId = userId,
                plantId = plant.plantId,
                tag = _selectedTag.value,
                plannedDuration = _selectedDuration.value,
                actualDuration = actualDuration,
                status = status,
                startedAt = sessionStartedAt ?: now,
                completedAt = if (status == "completed") now else null
            )

            if (status == "completed") {
                _lastEarnedCoins.value = response.coinsEarned
                _coins.value += response.coinsEarned
            }
        } catch (e: Exception) { }
    }

    // Sets duration and syncs remainingSeconds via totalSeconds()
    private fun updateDuration(value: Int) {
        if (_timerState.value !is TimerState.Idle) return
        _selectedDuration.value = value
        _remainingSeconds.value = totalSeconds()
        _progress.value = 1f
    }

    private fun resetToIdle() {
        _timerState.value = TimerState.Idle
        _remainingSeconds.value = totalSeconds()
        _progress.value = 1f
    }

    private fun enforceMinDuration(plant: Plant) {
        val min = if (devMode) plant.minDurationDev else plant.minDuration
        if (_selectedDuration.value < min) updateDuration(min)
    }

    private fun totalSeconds(): Int =
        if (devMode) _selectedDuration.value else _selectedDuration.value * 60

    private fun nowIso(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())
}
package pl.edu.pb.jardinito.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.manager.FocusSessionManager
import pl.edu.pb.jardinito.data.manager.WalletManager
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.model.Tag
import pl.edu.pb.jardinito.data.repository.PlantRepository
import pl.edu.pb.jardinito.data.repository.SessionRepository
import pl.edu.pb.jardinito.ui.service.FocusOverlayService
import pl.edu.pb.jardinito.viewmodel.state.TimerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

sealed class SessionResult {
    data class Completed(val plant: Plant, val coinsEarned: Int) : SessionResult()
    data class Failed(val plant: Plant) : SessionResult()
}

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val sessionRepository: SessionRepository,
    private val sessionManager: FocusSessionManager,
    private val walletManager: WalletManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // =====================
    // STATE
    // =====================

    val devMode = true

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState

    private val _selectedDuration = MutableStateFlow(5)
    val selectedDuration: StateFlow<Int> = _selectedDuration

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

    private val _sessionResult = MutableStateFlow<SessionResult?>(null)
    val sessionResult: StateFlow<SessionResult?> = _sessionResult

    private val _showStopConfirmDialog = MutableStateFlow(false)
    val showStopConfirmDialog: StateFlow<Boolean> = _showStopConfirmDialog

    private var timerJob: Job? = null
    private var sessionStartedAt: String? = null

    val unlockedPlantIds: StateFlow<Set<String>> = walletManager.unlockedPlantIdsFlow

    private var currentUserId: String = ""

    val coins: StateFlow<Int> = walletManager.coinsFlow

    // =====================
    // INIT
    // =====================

    init {
        loadPlants()
        observeBackgroundFail()
    }

    private fun observeBackgroundFail() {
        viewModelScope.launch {
            sessionManager.failSessionEvent.collect {
                if (currentUserId.isNotBlank()) {
                    stop(currentUserId)
                }
            }
        }
    }

    // =====================
    // ACTIONS
    // =====================

    fun loadPlants() {
        viewModelScope.launch {
            try {
                val result = plantRepository.getPlants()
                _plants.value = result
                val defaultPlant = result.firstOrNull { it.price == 0 } ?: result.firstOrNull()
                defaultPlant?.let { plant ->
                    _selectedPlant.value = plant
                    updateDuration(if (devMode) plant.minDurationDev else plant.minDuration)
                }
            } catch (e: Exception) {
                android.util.Log.e("FocusViewModel", "loadPlants failed: ${e.message}", e)
            }
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
        currentUserId = userId
        sessionStartedAt = nowIso()
        _timerState.value = TimerState.Running
        sessionManager.setTimerRunning(true)
        startFocusService()
        runTimer(userId)
    }

    fun pause() {
        timerJob?.cancel()
        _timerState.value = TimerState.Paused
        sessionManager.setTimerRunning(false)
    }

    fun resume(userId: String) {
        _timerState.value = TimerState.Running
        sessionManager.setTimerRunning(true)
        runTimer(userId)
    }

    private fun stop(userId: String) {
        timerJob?.cancel()
        sessionManager.setTimerRunning(false)
        stopFocusService()
        val plant = _selectedPlant.value
        if (plant != null && sessionStartedAt != null) {
            viewModelScope.launch {
                saveSession(userId, plant, status = "failed")
                resetToIdle()
                sessionStartedAt = null
            }
        } else {
            resetToIdle()
            sessionStartedAt = null
        }
    }

    fun requestStop() {
        pause()
        _showStopConfirmDialog.value = true
    }

    fun confirmStop(userId: String) {
        _showStopConfirmDialog.value = false
        stop(userId)
    }

    fun dismissStop(userId: String) {
        _showStopConfirmDialog.value = false
        resume(userId)
    }

    fun clearSessionResult() {
        _sessionResult.value = null
    }

    // =====================
    // PRIVATE HELPERS
    // =====================

    private fun runTimer(userId: String) {
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                _progress.value = _remainingSeconds.value.toFloat() / totalSeconds().toFloat()
            }
            delay(1000)
            val plant = _selectedPlant.value ?: return@launch
            sessionManager.run { setTimerRunning(false) }
            stopFocusService()
            saveSession(userId, plant, status = "completed")
            resetToIdle()
        }
    }

    private suspend fun saveSession(userId: String, plant: Plant, status: String) {
        try {
            val now = nowIso()
            val actualDuration = if (devMode)
                totalSeconds() - _remainingSeconds.value  // sekundy w dev
            else
                (totalSeconds() - _remainingSeconds.value) / 60
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
                walletManager.addCoins(response.coinsEarned)
                _sessionResult.value = SessionResult.Completed(plant, response.coinsEarned)
            } else {
                _sessionResult.value = SessionResult.Failed(plant)
            }
        } catch (e: Exception) {
            android.util.Log.e("FocusViewModel", "saveSession failed: ${e.message}", e)
        }
    }

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

    private fun totalSeconds(): Int =
        if (devMode) _selectedDuration.value else _selectedDuration.value * 60

    private fun nowIso(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())


    private fun startFocusService() {
        Intent(context, FocusOverlayService::class.java).apply {
            action = FocusOverlayService.ACTION_START
        }.also { context.startForegroundService(it) }
    }

    private fun stopFocusService() {
        Intent(context, FocusOverlayService::class.java).apply {
            action = FocusOverlayService.ACTION_STOP
        }.also { context.startService(it) }
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.setTimerRunning(false)
        stopFocusService()
    }
}
package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.viewmodel.state.TimerState
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor() : ViewModel() {

    // =====================
    // STATE
    // =====================

    private val devMode = true

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState

    private val _selectedDuration = MutableStateFlow(if (devMode) 5 else 15)
    val selectedDuration: StateFlow<Int> = _selectedDuration

    private val _remainingSeconds = MutableStateFlow(if (devMode) 5 else 15 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress

    private var timerJob: Job? = null

    // =====================
    // ACTIONS
    // =====================

    fun setDurationDev(seconds: Int) {
        if (_timerState.value is TimerState.Idle) {
            _selectedDuration.value = seconds
            _remainingSeconds.value = seconds
            _progress.value = 1f
        }
    }

    fun setDuration(minutes: Int) {
        if (_timerState.value is TimerState.Idle) {
            _selectedDuration.value = minutes
            _remainingSeconds.value = minutes * 60
            _progress.value = 1f
        }
    }

    fun pause() {
        timerJob?.cancel()
        _timerState.value = TimerState.Paused
    }

    private fun totalSeconds(): Int = if (devMode) _selectedDuration.value else _selectedDuration.value * 60

    fun start() {
        _timerState.value = TimerState.Running
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                _progress.value = _remainingSeconds.value.toFloat() / totalSeconds().toFloat()
            }
            delay(1000)
            _timerState.value = TimerState.Idle
            _remainingSeconds.value = totalSeconds()
            _progress.value = 1f
        }
    }

    fun resume() {
        _timerState.value = TimerState.Running
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                _progress.value = _remainingSeconds.value.toFloat() / totalSeconds().toFloat()
            }
            delay(1000)
            _timerState.value = TimerState.Idle
            _remainingSeconds.value = totalSeconds()
            _progress.value = 1f
        }
    }

    fun stop() {
        timerJob?.cancel()
        _timerState.value = TimerState.Idle
        _remainingSeconds.value = totalSeconds()
        _progress.value = 1f
    }
}
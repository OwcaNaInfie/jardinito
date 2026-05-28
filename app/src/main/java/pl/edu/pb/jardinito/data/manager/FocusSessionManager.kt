package pl.edu.pb.jardinito.data.manager

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionManager @Inject constructor() {

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    val failSessionEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun setTimerRunning(running: Boolean) {
        _isTimerRunning.value = running
    }

    fun triggerFail() {
        failSessionEvent.tryEmit(Unit)
    }
}
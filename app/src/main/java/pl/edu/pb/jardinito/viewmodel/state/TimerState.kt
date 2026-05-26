package pl.edu.pb.jardinito.viewmodel.state

sealed class TimerState {
    object Idle : TimerState()
    object Running : TimerState()
    object Paused : TimerState()
    object Finished : TimerState()
}
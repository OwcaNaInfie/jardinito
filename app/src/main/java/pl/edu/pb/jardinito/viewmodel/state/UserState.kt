package pl.edu.pb.jardinito.viewmodel.state

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    object Success : UserState()
    data class Error(val message: String) : UserState()
}
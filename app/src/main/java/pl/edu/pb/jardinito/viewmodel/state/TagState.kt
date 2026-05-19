package pl.edu.pb.jardinito.viewmodel.state

sealed class TagState {
    object Idle : TagState()
    object Loading : TagState()
    data class Error(val message: String) : TagState()
}
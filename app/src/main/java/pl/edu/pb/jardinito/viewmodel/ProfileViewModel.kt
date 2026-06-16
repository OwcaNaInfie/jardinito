package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.manager.WalletManager
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.repository.PlantRepository
import pl.edu.pb.jardinito.data.repository.WalletRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val walletManager: WalletManager
) : ViewModel() {

    val coins: StateFlow<Int> = walletManager.coinsFlow

    private val _favouritePlants = MutableStateFlow<List<Plant>>(emptyList())
    val favouritePlants: StateFlow<List<Plant>> = _favouritePlants

    init {
        viewModelScope.launch {
            walletManager.favouritePlantIdsFlow.collect { ids ->
                _favouritePlants.value = ids.mapNotNull { plantRepository.getPlantById(it) }
            }
        }
    }
}
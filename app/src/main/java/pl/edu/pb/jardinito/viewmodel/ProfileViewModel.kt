package pl.edu.pb.jardinito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.repository.PlantRepository
import pl.edu.pb.jardinito.data.repository.WalletRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val plantRepository: PlantRepository
) : ViewModel() {

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins

    private val _favouritePlants = MutableStateFlow<List<Plant>>(emptyList())
    val favouritePlants: StateFlow<List<Plant>> = _favouritePlants

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun load(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val wallet = walletRepository.getWallet(userId)
                _coins.value = wallet.coins

                val allPlants = plantRepository.getPlants()
                _favouritePlants.value = allPlants.filter {
                    wallet.favouritePlantIds.contains(it.plantId)
                }
            } catch (e: Exception) { } finally {
                _isLoading.value = false
            }
        }
    }
}
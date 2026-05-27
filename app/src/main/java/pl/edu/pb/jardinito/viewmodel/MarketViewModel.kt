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

sealed class MarketError {
    data object InsufficientCoins : MarketError()
    data object AlreadyUnlocked : MarketError()
    data object NetworkError : MarketError()
}

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins

    private val _unlockedPlantIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedPlantIds: StateFlow<Set<String>> = _unlockedPlantIds

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<MarketError?>(null)
    val error: StateFlow<MarketError?> = _error

    private val _buySuccess = MutableStateFlow<Plant?>(null)
    val buySuccess: StateFlow<Plant?> = _buySuccess

    private val _favouritePlantIds = MutableStateFlow<Set<String>>(emptySet())
    val favouritePlantIds: StateFlow<Set<String>> = _favouritePlantIds

    private var currentUserId: String = ""

    fun loadPlants() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _plants.value = plantRepository.getPlants()
            } catch (e: Exception) {
                _error.value = MarketError.NetworkError
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadWallet(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            try {
                val wallet = walletRepository.getWallet(userId)
                _coins.value = wallet.coins
                _unlockedPlantIds.value = wallet.unlockedPlantIds.toSet()
                _favouritePlantIds.value = wallet.favouritePlantIds.toSet()
            } catch (e: Exception) {
                _error.value = MarketError.NetworkError
            }
        }
    }

    fun getPlantById(plantId: String): Plant? {
        return _plants.value.firstOrNull { it.plantId == plantId }
    }

    fun buyPlant(plant: Plant) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val wallet = walletRepository.buyPlant(currentUserId, plant.plantId)
                _coins.value = wallet.coins
                _unlockedPlantIds.value = wallet.unlockedPlantIds.toSet()
                _buySuccess.value = plant
            } catch (e: retrofit2.HttpException) {
                _error.value = when (e.code()) {
                    400 -> {
                        val body = e.response()?.errorBody()?.string() ?: ""
                        if (body.contains("already")) MarketError.AlreadyUnlocked
                        else MarketError.InsufficientCoins
                    }
                    else -> MarketError.NetworkError
                }
            } catch (e: Exception) {
                _error.value = MarketError.NetworkError
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavourite(plantId: String) {
        viewModelScope.launch {
            try {
                val wallet = walletRepository.toggleFavourite(currentUserId, plantId)
                _favouritePlantIds.value = wallet.favouritePlantIds.toSet()
            } catch (e: Exception) {
                _error.value = MarketError.NetworkError
            }
        }
    }

    fun clearError() { _error.value = null }
    fun clearBuySuccess() { _buySuccess.value = null }
}
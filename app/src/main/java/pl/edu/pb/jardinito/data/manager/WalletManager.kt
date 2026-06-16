package pl.edu.pb.jardinito.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.edu.pb.jardinito.data.remote.WalletApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletManager @Inject constructor() {

    private val _coinsFlow = MutableStateFlow(0)
    val coinsFlow: StateFlow<Int> = _coinsFlow

    private val _unlockedPlantIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    val unlockedPlantIdsFlow: StateFlow<Set<String>> = _unlockedPlantIdsFlow

    private val _favouritePlantIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    val favouritePlantIdsFlow: StateFlow<Set<String>> = _favouritePlantIdsFlow

    fun updateFromResponse(response: WalletApiService.WalletResponse) {
        _coinsFlow.value = response.coins
        _unlockedPlantIdsFlow.value = response.unlockedPlantIds.orEmpty().toSet()
        _favouritePlantIdsFlow.value = response.favouritePlantIds.orEmpty().toSet()
    }

    fun addCoins(amount: Int) { _coinsFlow.value += amount }
}
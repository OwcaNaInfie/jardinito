package pl.edu.pb.jardinito.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletManager @Inject constructor() {
    private val _coinsFlow = MutableStateFlow(0)
    val coinsFlow: StateFlow<Int> = _coinsFlow

    fun updateCoins(value: Int) { _coinsFlow.value = value }
    fun addCoins(amount: Int)   { _coinsFlow.value += amount }
}
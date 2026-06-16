package pl.edu.pb.jardinito.viewmodel

import android.content.Context
import pl.edu.pb.jardinito.data.repository.WalletRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.edu.pb.jardinito.data.manager.WalletManager
import pl.edu.pb.jardinito.data.model.Plant
import pl.edu.pb.jardinito.data.repository.PlantRepository
import pl.edu.pb.jardinito.ui.utils.PlantColor
import pl.edu.pb.jardinito.ui.utils.PlantOwnershipStatus
import pl.edu.pb.jardinito.ui.utils.PlantSize
import pl.edu.pb.jardinito.ui.utils.PriceSortOrder
import pl.edu.pb.jardinito.ui.utils.applyFilters
import pl.edu.pb.jardinito.ui.utils.resolveString
import javax.inject.Inject

sealed class MarketError {
    data object InsufficientCoins : MarketError()
    data object AlreadyUnlocked   : MarketError()
    data object NetworkError      : MarketError()
}

data class MarketFilterState(
    val searchQuery: String          = "",
    val filterColors: Set<PlantColor>  = emptySet(),
    val filterSizes: Set<PlantSize>    = emptySet(),
    val filterStatus: PlantOwnershipStatus? = null,
    val priceSortOrder: PriceSortOrder?     = null
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank()
                || filterColors.isNotEmpty()
                || filterSizes.isNotEmpty()
                || (filterStatus != null && filterStatus != PlantOwnershipStatus.ALL)
                || priceSortOrder != null
}

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val walletRepository: WalletRepository,
    private val walletManager: WalletManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    val unlockedPlantIds: StateFlow<Set<String>> = walletManager.unlockedPlantIdsFlow

    val favouritePlantIds: StateFlow<Set<String>> = walletManager.favouritePlantIdsFlow

    val coins: StateFlow<Int> = walletManager.coinsFlow

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<MarketError?>(null)
    val error: StateFlow<MarketError?> = _error

    private val _buySuccess = MutableStateFlow<Plant?>(null)
    val buySuccess: StateFlow<Plant?> = _buySuccess

    private val _filterState = MutableStateFlow(MarketFilterState())
    val filterState: StateFlow<MarketFilterState> = _filterState

    val filteredPlants: StateFlow<List<Plant>> = combine(
        _plants, walletManager.unlockedPlantIdsFlow, _filterState
    ) { plants, unlockedIds, filters ->
        val resolvedNames = plants.associate {
            it.plantId to resolveString(context, it.nameKey)
        }
        plants.applyFilters(
            query          = filters.searchQuery,
            colors         = filters.filterColors,
            sizes          = filters.filterSizes,
            status         = filters.filterStatus,
            unlockedIds    = unlockedIds,
            priceSortOrder = filters.priceSortOrder,
            resolvedNames  = resolvedNames
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var currentUserId: String = ""

    // ---- Data loading ----

    fun setUserId(userId: String) {
        currentUserId = userId
    }

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
                walletRepository.getWallet(userId)
            } catch (e: Exception) {
                _error.value = MarketError.NetworkError
            }
        }
    }
    // ---- Purchases ----

    fun buyPlant(plant: Plant) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                walletRepository.buyPlant(currentUserId, plant.plantId)
                _buySuccess.value = plant
            } catch (e: retrofit2.HttpException) {
                android.util.Log.e("MarketViewModel", "buyPlant HttpException: ${e.code()} ${e.message()}")
                _error.value = when (e.code()) {
                    400 -> {
                        val body = e.response()?.errorBody()?.string() ?: ""
                        if (body.contains("already")) MarketError.AlreadyUnlocked
                        else MarketError.InsufficientCoins
                    }
                    else -> MarketError.NetworkError
                }
            } catch (e: Exception) {
                android.util.Log.e("MarketViewModel", "buyPlant Exception: ${e::class.simpleName} ${e.message}")
                _error.value = MarketError.NetworkError
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavourite(plantId: String) {
        viewModelScope.launch {
            try {
                walletRepository.toggleFavourite(currentUserId, plantId)
            } catch (e: Exception) {
                _error.value = MarketError.NetworkError
            }
        }
    }

    // ---- Filter actions ----

    fun updateSearchQuery(query: String) =
        _filterState.update { it.copy(searchQuery = query) }

    fun updateFilterColors(colors: Set<PlantColor>) =
        _filterState.update { it.copy(filterColors = colors) }

    fun updateFilterSizes(sizes: Set<PlantSize>) =
        _filterState.update { it.copy(filterSizes = sizes) }

    fun updateFilterStatus(status: PlantOwnershipStatus?) =
        _filterState.update { it.copy(filterStatus = status) }

    fun togglePriceSortOrder() = _filterState.update { state ->
        state.copy(
            priceSortOrder = when (state.priceSortOrder) {
                null                     -> PriceSortOrder.ASCENDING
                PriceSortOrder.ASCENDING  -> PriceSortOrder.DESCENDING
                PriceSortOrder.DESCENDING -> null
            }
        )
    }

    fun clearFilters() { _filterState.value = MarketFilterState() }

    fun clearError()      { _error.value = null }
    fun clearBuySuccess() { _buySuccess.value = null }
}
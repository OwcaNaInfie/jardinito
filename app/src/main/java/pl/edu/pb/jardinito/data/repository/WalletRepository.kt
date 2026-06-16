package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.manager.WalletManager
import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.data.remote.WalletApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val walletManager: WalletManager
) {
    private val api = RetrofitInstance.wallet

    suspend fun getWallet(userId: String): WalletApiService.WalletResponse =
        api.getWallet(userId).also { walletManager.updateFromResponse(it) }

    suspend fun buyPlant(userId: String, plantId: String): WalletApiService.WalletResponse =
        api.buyPlant(WalletApiService.BuyPlantRequest(userId, plantId))
            .also { walletManager.updateFromResponse(it) }

    suspend fun toggleFavourite(userId: String, plantId: String): WalletApiService.WalletResponse =
        api.toggleFavourite(WalletApiService.FavouriteRequest(userId, plantId))
            .also { walletManager.updateFromResponse(it) }
}
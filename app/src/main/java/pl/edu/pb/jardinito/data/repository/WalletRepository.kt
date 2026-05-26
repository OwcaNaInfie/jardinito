package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import pl.edu.pb.jardinito.data.remote.WalletApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor() {

    private val api = RetrofitInstance.wallet

    suspend fun getCoins(userId: String): Int {
        return api.getWallet(userId).coins
    }

    suspend fun getWallet(userId: String): WalletApiService.WalletResponse {
        return api.getWallet(userId)
    }

    suspend fun buyPlant(userId: String, plantId: String): WalletApiService.WalletResponse {
        return api.buyPlant(WalletApiService.BuyPlantRequest(userId, plantId))
    }
}
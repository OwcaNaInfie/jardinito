package pl.edu.pb.jardinito.data.repository

import pl.edu.pb.jardinito.data.remote.RetrofitInstance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor() {

    private val api = RetrofitInstance.wallet

    suspend fun getCoins(userId: String): Int {
        return api.getWallet(userId).coins
    }
}
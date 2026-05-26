package pl.edu.pb.jardinito.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WalletApiService {

    @GET("api/wallet")
    suspend fun getWallet(@Query("userId") userId: String): WalletResponse

    data class WalletResponse(val coins: Int)
}
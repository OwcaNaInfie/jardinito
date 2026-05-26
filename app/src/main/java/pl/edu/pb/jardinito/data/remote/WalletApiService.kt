package pl.edu.pb.jardinito.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WalletApiService {

    @GET("api/wallet")
    suspend fun getWallet(@Query("userId") userId: String): WalletResponse

    @POST("api/wallet/buy")
    suspend fun buyPlant(@Body request: BuyPlantRequest): WalletResponse

    data class WalletResponse(
        val coins: Int,
        val unlockedPlantIds: List<String>
    )

    data class BuyPlantRequest(
        val userId: String,
        val plantId: String
    )
}
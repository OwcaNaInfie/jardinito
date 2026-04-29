package  pl.edu.pb.jardinito.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

//    const val BASE_URL = "http://10.0.2.2:5000/"

    // For mobile connection
    const val BASE_URL = "http://10.115.66.240:5000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

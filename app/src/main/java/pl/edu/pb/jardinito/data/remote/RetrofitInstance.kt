package pl.edu.pb.jardinito.data.remote

import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    const val BASE_URL = "http://192.168.0.28:5000/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (!response.isSuccessful) {
                val bodyString = response.body?.string() ?: ""
                response.newBuilder()
                    .body(bodyString.toResponseBody(response.body?.contentType()))
                    .build()
            } else {
                response
            }
        }
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val auth: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val user: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val tags: TagApiService by lazy { retrofit.create(TagApiService::class.java) }
}
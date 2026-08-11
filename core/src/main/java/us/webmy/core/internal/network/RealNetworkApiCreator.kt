package us.webmy.core.internal.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.webmy.core.network.NetworkApiCreator

internal class RealNetworkApiCreator(
    private val okHttpClient: OkHttpClient,
) : NetworkApiCreator {

    override fun <T> create(
        service: Class<T>,
        baseUrl: String,
    ): T {
        return createRetrofit(baseUrl).create(service)
    }

    override fun createRetrofit(baseUrl: String, customOkHttpClient: OkHttpClient?): Retrofit {
        return Retrofit.Builder()
            .client(customOkHttpClient ?: okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

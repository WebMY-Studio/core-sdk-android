package us.webmy.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface NetworkApiCreator {
    fun <T> create(service: Class<T>, baseUrl: String): T

    fun createRetrofit(baseUrl: String, customOkHttpClient: OkHttpClient? = null): Retrofit
}

inline fun <reified T> NetworkApiCreator.create(baseUrl: String): T {
    return create(T::class.java, baseUrl)
}

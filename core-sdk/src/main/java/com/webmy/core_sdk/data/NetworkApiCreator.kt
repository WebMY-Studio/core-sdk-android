package com.webmy.core_sdk.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

interface NetworkApiCreator {
    fun <T> create(service: Class<T>, baseUrl: String): T

    fun createRetrofit(baseUrl: String, customOkHttpClient: OkHttpClient? = null): Retrofit
}

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

inline fun <reified T> NetworkApiCreator.create(baseUrl: String): T {
    return create(T::class.java, baseUrl)
}

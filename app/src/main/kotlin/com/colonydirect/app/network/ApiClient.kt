package com.colonydirect.app.network

import com.colonydirect.app.data.TokenStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central Retrofit/OkHttp client factory.
 *
 * Created once in ColonyDirectApp and shared via the ServiceLocator.
 * Not using Hilt in this step to keep the build configuration minimal;
 * Hilt will be introduced in Step 4 (Dashboards & Polish) when the full
 * multi-module DI graph is needed.
 */
object ApiClient {

    val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

    private fun buildOkHttpClient(tokenStore: TokenStore): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun buildRetrofit(tokenStore: TokenStore): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(buildOkHttpClient(tokenStore))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}

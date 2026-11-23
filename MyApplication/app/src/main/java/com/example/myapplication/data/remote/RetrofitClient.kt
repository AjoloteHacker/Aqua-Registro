package com.example.myapplication.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 es la dirección IP virtual del host en el emulador estándar de Android Studio.
    // Para probar en teléfono físico con cable USB / reverse port: `adb reverse tcp:8080 tcp:8080` (usa localhost:8080).
    var BASE_URL_AQUACHECK: String = "http://10.0.2.2:8080/api/"
    private const val BASE_URL_CLIMA: String = "https://api.open-meteo.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val aquaApi: AquaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_AQUACHECK)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AquaApiService::class.java)
    }

    val climaApi: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CLIMA)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }
}

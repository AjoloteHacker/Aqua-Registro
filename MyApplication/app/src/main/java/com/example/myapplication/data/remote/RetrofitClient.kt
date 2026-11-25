package com.example.myapplication.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * URL pública HTTPS de ajoloserver mediante Tailscale Funnel (accesible desde cualquier
     * teléfono físico por 4G/5G o cualquier Wi-Fi sin necesidad de estar en la misma red).
     */
    const val URL_AJOLOSERVER_PUBLICA: String = "https://ajoloserver.tail19de00.ts.net:8443/api/"

    /** URL directa de ajoloserver dentro de la red Tailscale. */
    const val URL_AJOLOSERVER_TAILSCALE: String = "http://100.124.162.2:18080/api/"

    /** URL directa de ajoloserver en red local Wi-Fi (LAN). */
    const val URL_AJOLOSERVER_LAN: String = "http://192.168.4.143:18080/api/"

    /** URL para pruebas locales por cable USB (`adb reverse tcp:8080 tcp:8080`). */
    const val URL_USB_LOCALHOST: String = "http://127.0.0.1:8080/api/"

    /** URL estándar del Emulador de Android Studio hacia el `localhost:8080` del PC anfitrión. */
    const val URL_EMULADOR_LOCAL: String = "http://10.0.2.2:8080/api/"

    @Volatile
    var BASE_URL_AQUACHECK: String = URL_AJOLOSERVER_PUBLICA
        private set

    private const val BASE_URL_CLIMA: String = "https://api.open-meteo.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var aquaApiInstance: AquaApiService = crearAquaApi(BASE_URL_AQUACHECK)

    val aquaApi: AquaApiService
        get() = aquaApiInstance

    private fun crearAquaApi(baseUrl: String): AquaApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AquaApiService::class.java)
    }

    /**
     * Normaliza y actualiza dinámicamente la URL base del servidor Spring Boot (ajoloserver)
     * reconstruyendo el cliente Retrofit sin necesidad de recompilar la app.
     */
    @Synchronized
    fun actualizarBaseUrl(nuevaUrl: String): String {
        val limpia = normalizarBaseUrl(nuevaUrl)
        BASE_URL_AQUACHECK = limpia
        aquaApiInstance = crearAquaApi(limpia)
        return limpia
    }

    fun normalizarBaseUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (url.isBlank()) return URL_AJOLOSERVER_PUBLICA
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "https://$url"
        }
        url = url.trimEnd('/')
        url = if (url.endsWith("/api", ignoreCase = true)) {
            "$url/"
        } else {
            "$url/api/"
        }
        return url
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

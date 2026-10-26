package com.example.myapplication.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

/**
 * Generador de fotos de perfil usando el servicio Random User (randomuser.me).
 */
object RandomUserGenerator {

    private const val API_URL = "https://randomuser.me/api/?inc=picture&noinfo"

    /**
     * Consulta la API de Random User para obtener una foto de perfil real.
     * Si no hay conexión o falla la red, recurre de forma transparente al catálogo de retratos de Random User.
     */
    suspend fun obtenerFotoAleatoria(genero: String? = null): String = withContext(Dispatchers.IO) {
        try {
            val urlString = if (genero != null) "$API_URL&gender=$genero" else API_URL
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "AquaRegistro/1.0")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val results = root.getJSONArray("results")
                if (results.length() > 0) {
                    val picture = results.getJSONObject(0).getJSONObject("picture")
                    val large = picture.optString("large")
                    if (large.isNotBlank()) {
                        return@withContext large
                    }
                }
            }
        } catch (_: Exception) {
            // Manejo silencioso ante desconexión o timeout
        }

        generarUrlFallback(genero)
    }

    /**
     * Genera una URL directa al repositorio de retratos de Random User.
     */
    fun generarUrlFallback(genero: String? = null): String {
        val gen = genero ?: "men"
        val id = Random.nextInt(1, 95)
        return "https://randomuser.me/api/portraits/$gen/$id.jpg"
    }
}

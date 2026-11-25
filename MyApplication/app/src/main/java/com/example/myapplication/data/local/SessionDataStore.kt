package com.example.myapplication.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aqua_check_preferences")

class SessionDataStore(private val context: Context) {

    companion object {
        val KEY_USER_RUT = stringPreferencesKey("user_rut")
        val KEY_USER_NOMBRE = stringPreferencesKey("user_nombre")
        val KEY_USER_CENTRO = stringPreferencesKey("user_centro")
        val KEY_ULTIMA_SYNC = stringPreferencesKey("ultima_sincronizacion")
        val KEY_PENDIENTES_COUNT = intPreferencesKey("pendientes_sincronizar_count")
        val KEY_SERVER_URL = stringPreferencesKey("server_base_url")
    }

    val userRutFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USER_RUT] }
    val userNombreFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USER_NOMBRE] }
    val userCentroFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USER_CENTRO] }
    val ultimaSyncFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ULTIMA_SYNC] }
    val pendientesCountFlow: Flow<Int> = context.dataStore.data.map { it[KEY_PENDIENTES_COUNT] ?: 0 }
    val serverUrlFlow: Flow<String?> = context.dataStore.data.map { it[KEY_SERVER_URL] }

    suspend fun guardarServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SERVER_URL] = url
        }
    }

    suspend fun guardarSesion(rut: String, nombre: String, centro: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_RUT] = rut
            preferences[KEY_USER_NOMBRE] = nombre
            preferences[KEY_USER_CENTRO] = centro
        }
    }

    suspend fun registrarSincronizacionExitosa(fechaHora: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ULTIMA_SYNC] = fechaHora
            preferences[KEY_PENDIENTES_COUNT] = 0
        }
    }

    suspend fun actualizarPendientes(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PENDIENTES_COUNT] = count
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_USER_RUT)
            preferences.remove(KEY_USER_NOMBRE)
            preferences.remove(KEY_USER_CENTRO)
        }
    }
}

package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.repository.AquaRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel para la pantalla principal (MenuScreen).
 * Gestiona el estado reactivo del menú y desacopla completamente la interfaz del Repositorio (MVVM).
 */
class MenuViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {
    val prevencionistaActivo: StateFlow<Prevencionista?> = repository.prevencionistaActivo
    val buzos: StateFlow<List<Buzo>> = repository.buzos
    val faenas: StateFlow<List<RegistroFaena>> = repository.faenas
    val isNetworkConnected: StateFlow<Boolean> = repository.isNetworkConnected
    val climaMaritimo: StateFlow<String> = repository.climaMaritimo
    val syncStatus: StateFlow<String> = repository.syncStatus
    val pendientesSincronizarIds: StateFlow<Set<String>> = repository.pendientesSincronizarIds
    val serverUrl: StateFlow<String> = repository.serverUrl
    val servidorOnline: StateFlow<Boolean> = repository.servidorOnline
    val ultimoPingDetalle: StateFlow<String> = repository.ultimoPingDetalle

    fun sincronizarAhora() {
        repository.sincronizarAhora()
    }

    fun alternarModoOfflineDemo() {
        repository.alternarModoOfflineDemo()
    }

    fun cambiarUrlServidor(nuevaUrl: String) {
        repository.cambiarUrlServidor(nuevaUrl)
    }

    fun probarConexionServidor() {
        repository.probarConexionServidor()
    }

    fun actualizarClima(centro: String) {
        repository.consultarClimaMaritimo(centro)
    }

    fun logout() {
        repository.logout()
    }
}

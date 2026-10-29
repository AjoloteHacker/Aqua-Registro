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

    fun logout() {
        repository.logout()
    }
}

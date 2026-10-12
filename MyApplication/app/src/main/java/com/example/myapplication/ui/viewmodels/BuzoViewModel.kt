package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.repository.AquaRepository
import com.example.myapplication.util.Rutificador
import com.example.myapplication.util.SecurityUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BuzoUiState(
    val buzos: List<Buzo> = emptyList(),
    val searchQuery: String = "",
    val filtroSalud: EstadoSalud? = null,
    val showAddDialog: Boolean = false,
    val buzoAEditar: Buzo? = null,
    val buzoDetalle: Buzo? = null,
    val buzoAEliminar: Buzo? = null,
    val nuevoNombre: String = "",
    val nuevoRut: String = "",
    val nuevaMatricula: String = "",
    val nuevaCategoria: String = "Buzo Comercial",
    val nuevoTelefono: String = "",
    val nuevaVigencia: String = "Al día",
    val empresaSeleccionada: String = "",
    val empresasDisponibles: List<String> = emptyList(),
    val showNuevaEmpresaDialog: Boolean = false,
    val nuevaEmpresaNombre: String = "",
    val userMessage: String? = null,
    val error: String? = null
) {
    val buzosFiltrados: List<Buzo>
        get() = buzos.filter { buzo ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                buzo.nombre.contains(searchQuery, ignoreCase = true) ||
                buzo.rut.contains(searchQuery, ignoreCase = true) ||
                buzo.empresa.contains(searchQuery, ignoreCase = true) ||
                buzo.matricula.contains(searchQuery, ignoreCase = true) ||
                buzo.categoria.contains(searchQuery, ignoreCase = true)
            }
            val matchesSalud = filtroSalud == null || buzo.estadoSalud == filtroSalud
            matchesQuery && matchesSalud
        }
}

// Autor: Amaro Villalobos
class BuzoViewModel(
    private val repository: AquaRepository = AquaRepository.getInstance()
) : ViewModel() {

    val categoriasDisponibles = listOf(
        "Buzo Comercial",
        "Buzo Básico",
        "Buzo Intermedio",
        "Buzo Especialista",
        "Supervisor de Buceo"
    )

    val vigenciasDisponibles = listOf(
        "Al día",
        "Por renovar (< 30 días)",
        "Expirada"
    )

    private val _uiState = MutableStateFlow(
        BuzoUiState(
            empresasDisponibles = repository.empresas,
            empresaSeleccionada = repository.empresas.firstOrNull() ?: ""
        )
    )
    val uiState: StateFlow<BuzoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.buzos.collect { lista ->
                _uiState.update { state ->
                    // Mantener sincronizado buzoDetalle si está abierto
                    val detalleActualizado = state.buzoDetalle?.let { detalle ->
                        lista.firstOrNull { it.id == detalle.id }
                    }
                    state.copy(
                        buzos = lista,
                        buzoDetalle = detalleActualizado
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.empresasFlow.collect { empresas ->
                _uiState.update { state ->
                    state.copy(
                        empresasDisponibles = empresas,
                        empresaSeleccionada = if (state.empresaSeleccionada.isBlank()) {
                            empresas.firstOrNull() ?: ""
                        } else {
                            state.empresaSeleccionada
                        }
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFiltroSaludChange(filtro: EstadoSalud?) {
        _uiState.update { it.copy(filtroSalud = filtro) }
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                buzoAEditar = null,
                nuevoNombre = "",
                nuevoRut = "",
                nuevaMatricula = "",
                nuevaCategoria = categoriasDisponibles.first(),
                nuevoTelefono = "",
                nuevaVigencia = vigenciasDisponibles.first(),
                empresaSeleccionada = it.empresasDisponibles.firstOrNull() ?: "",
                error = null
            )
        }
    }

    fun openEditDialog(buzo: Buzo) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                buzoAEditar = buzo,
                nuevoNombre = buzo.nombre,
                nuevoRut = buzo.rut,
                nuevaMatricula = buzo.matricula,
                nuevaCategoria = if (buzo.categoria in categoriasDisponibles) buzo.categoria else categoriasDisponibles.first(),
                nuevoTelefono = buzo.telefono,
                nuevaVigencia = if (buzo.vigenciaMatricula in vigenciasDisponibles) buzo.vigenciaMatricula else vigenciasDisponibles.first(),
                empresaSeleccionada = buzo.empresa,
                error = null
            )
        }
    }

    fun closeAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                buzoAEditar = null,
                nuevoNombre = "",
                nuevoRut = "",
                nuevaMatricula = "",
                nuevoTelefono = "",
                error = null
            )
        }
    }

    fun openDetalle(buzo: Buzo) {
        _uiState.update { it.copy(buzoDetalle = buzo) }
    }

    fun closeDetalle() {
        _uiState.update { it.copy(buzoDetalle = null) }
    }

    fun solicitarEliminar(buzo: Buzo) {
        _uiState.update { it.copy(buzoAEliminar = buzo) }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(buzoAEliminar = null) }
    }

    fun confirmarEliminar() {
        val buzo = _uiState.value.buzoAEliminar ?: return
        val result = repository.eliminarBuzo(buzo.id)
        result.onSuccess {
            _uiState.update {
                it.copy(
                    buzoAEliminar = null,
                    buzoDetalle = if (it.buzoDetalle?.id == buzo.id) null else it.buzoDetalle,
                    userMessage = "Buzo ${buzo.nombre} eliminado exitosamente."
                )
            }
        }.onFailure { err ->
            _uiState.update {
                it.copy(
                    buzoAEliminar = null,
                    error = err.message ?: "No fue posible eliminar al buzo."
                )
            }
        }
    }

    fun onNuevoNombreChange(v: String) = _uiState.update { it.copy(nuevoNombre = v, error = null) }
    fun onNuevoRutChange(v: String) {
        val filtrado = v.filter { it.isDigit() || it.equals('k', ignoreCase = true) || it == '.' || it == '-' }
        _uiState.update { it.copy(nuevoRut = filtrado, error = null) }
    }
    fun onNuevaMatriculaChange(v: String) = _uiState.update { it.copy(nuevaMatricula = v, error = null) }
    fun onNuevaCategoriaChange(v: String) = _uiState.update { it.copy(nuevaCategoria = v, error = null) }
    fun onNuevoTelefonoChange(v: String) = _uiState.update { it.copy(nuevoTelefono = v, error = null) }
    fun onNuevaVigenciaChange(v: String) = _uiState.update { it.copy(nuevaVigencia = v, error = null) }
    fun onEmpresaSeleccionadaChange(v: String) = _uiState.update { it.copy(empresaSeleccionada = v) }

    fun openNuevaEmpresaDialog() = _uiState.update { it.copy(showNuevaEmpresaDialog = true, nuevaEmpresaNombre = "") }
    fun closeNuevaEmpresaDialog() = _uiState.update { it.copy(showNuevaEmpresaDialog = false, nuevaEmpresaNombre = "") }
    fun onNuevaEmpresaNombreChange(v: String) = _uiState.update { it.copy(nuevaEmpresaNombre = v) }

    fun guardarNuevaEmpresa() {
        val nombre = _uiState.value.nuevaEmpresaNombre.trim()
        val result = repository.agregarEmpresa(nombre)
        result.onSuccess { empresaCreada ->
            _uiState.update {
                it.copy(
                    showNuevaEmpresaDialog = false,
                    nuevaEmpresaNombre = "",
                    empresaSeleccionada = empresaCreada,
                    userMessage = "Empresa '$empresaCreada' agregada a los registros."
                )
            }
        }.onFailure { err ->
            _uiState.update { it.copy(error = err.message) }
        }
    }

    fun guardarBuzo() {
        val state = _uiState.value

        val (nombreOk, nombreError) = SecurityUtils.validarNombre(state.nuevoNombre)
        if (!nombreOk) {
            _uiState.update { it.copy(error = nombreError) }
            return
        }

        val (rutOk, rutError) = SecurityUtils.validarRut(state.nuevoRut)
        if (!rutOk) {
            _uiState.update { it.copy(error = rutError) }
            return
        }

        val (telOk, telError) = SecurityUtils.validarTelefono(state.nuevoTelefono)
        if (!telOk) {
            _uiState.update { it.copy(error = telError) }
            return
        }

        val rutFormateado = Rutificador.formatear(state.nuevoRut)
        val matriculaFinal = if (state.nuevaMatricula.isBlank()) {
            "${state.nuevaCategoria} N° ${1000 + state.buzos.size}"
        } else {
            state.nuevaMatricula.trim()
        }
        val telefonoFinal = if (state.nuevoTelefono.isBlank()) "+56 9 9000 0000" else state.nuevoTelefono.trim()
        val empresaFinal = if (state.empresaSeleccionada.isBlank()) {
            state.empresasDisponibles.firstOrNull() ?: "Empresa Contratista"
        } else {
            state.empresaSeleccionada
        }

        val buzoAEditar = state.buzoAEditar

        if (buzoAEditar == null) {
            // MODO CREAR
            val nuevo = Buzo(
                id = (state.buzos.size + 1).toString(),
                rut = rutFormateado,
                nombre = state.nuevoNombre.trim(),
                empresa = empresaFinal,
                matricula = matriculaFinal,
                telefono = telefonoFinal,
                categoria = state.nuevaCategoria,
                vigenciaMatricula = state.nuevaVigencia,
                estadoSalud = EstadoSalud.PENDIENTE,
                ultimoChequeo = "Pendiente de faena"
            )

            val result = repository.agregarBuzo(nuevo)
            result.onSuccess { buzoAgregado ->
                _uiState.update {
                    it.copy(
                        showAddDialog = false,
                        nuevoNombre = "",
                        nuevoRut = "",
                        nuevaMatricula = "",
                        nuevoTelefono = "",
                        userMessage = "Buzo ${buzoAgregado.nombre} enrolado exitosamente.",
                        error = null
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(error = err.message ?: "Error al enrolar buzo") }
            }
        } else {
            // MODO EDITAR
            val actualizado = buzoAEditar.copy(
                rut = rutFormateado,
                nombre = state.nuevoNombre.trim(),
                empresa = empresaFinal,
                matricula = matriculaFinal,
                telefono = telefonoFinal,
                categoria = state.nuevaCategoria,
                vigenciaMatricula = state.nuevaVigencia
            )

            val result = repository.editarBuzo(actualizado)
            result.onSuccess { buzoEditado ->
                _uiState.update {
                    it.copy(
                        showAddDialog = false,
                        buzoAEditar = null,
                        nuevoNombre = "",
                        nuevoRut = "",
                        nuevaMatricula = "",
                        nuevoTelefono = "",
                        buzoDetalle = if (it.buzoDetalle?.id == buzoEditado.id) buzoEditado else it.buzoDetalle,
                        userMessage = "Ficha de ${buzoEditado.nombre} actualizada correctamente.",
                        error = null
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(error = err.message ?: "Error al actualizar buzo") }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}

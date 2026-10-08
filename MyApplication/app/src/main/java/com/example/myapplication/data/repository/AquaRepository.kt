package com.example.myapplication.data.repository

import com.example.myapplication.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.RevisionEquipo

/**
 * Repositorio central de la aplicación.
 */
class AquaRepository private constructor() {

    // Catálogos
    val empresas: List<String> = listOf(
        "Servicios Submarinos Austral SpA",
        "AquaSub Chile Limitada",
        "Buceos del Sur S.A.",
        "Patagonia Diving Services"
    )

    val centrosCultivo: List<String> = listOf(
        "Centro Melinka 1 - Canal Moraleda",
        "Centro Chiloé Norte - Isla Lemuy",
        "Centro Aysén 4 - Fiordo Aisén",
        "Centro Calbuco - Isla Huapi"
    )

    val tiposFaena: List<String> = listOf(
        "Inspección de redes loberas",
        "Limpieza y retiro de bioincrustaciones en mallas",
        "Extracción segura de mortalidad",
        "Inspección y tensado de fondeos submarinos",
        "Reparación de pasadores y tensores de jaulas"
    )

    // Estados
    private val _usuarios = MutableStateFlow<List<Prevencionista>>(
        listOf(
            Prevencionista(
                nombre = "Cristóbal Loncón C.",
                rut = "12.345.678-9",
                correo = "c.loncon@aquachile.com",
                registroSeremi = "PR-10892-X",
                centro = "Centro Melinka 1 - Canal Moraleda",
                contrasena = "admin123"
            )
        )
    )
    val usuarios: StateFlow<List<Prevencionista>> = _usuarios.asStateFlow()

    private val _prevencionistaActivo = MutableStateFlow<Prevencionista?>(_usuarios.value.first())
    val prevencionistaActivo: StateFlow<Prevencionista?> = _prevencionistaActivo.asStateFlow()

    private val _buzos = MutableStateFlow<List<Buzo>>(
        listOf(
            Buzo(
                id = "1",
                rut = "15.489.231-4",
                nombre = "Carlos Soto Paredes",
                empresa = "Servicios Submarinos Austral SpA",
                matricula = "Buzo Comercial N° 2394",
                telefono = "+56 9 8451 2290",
                estadoSalud = EstadoSalud.APTO,
                ultimoChequeo = "15/09/2026 - 08:30 hrs"
            ),
            Buzo(
                id = "2",
                rut = "17.912.445-K",
                nombre = "Pedro Almonacid Vera",
                empresa = "AquaSub Chile Limitada",
                matricula = "Buzo Básico N° 5812",
                telefono = "+56 9 7123 9940",
                estadoSalud = EstadoSalud.PENDIENTE,
                ultimoChequeo = "Pendiente de faena"
            ),
            Buzo(
                id = "3",
                rut = "16.320.108-7",
                nombre = "Rodrigo Mansilla Cárdenas",
                empresa = "Buceos del Sur S.A.",
                matricula = "Buzo Especialista N° 1042",
                telefono = "+56 9 9234 5512",
                estadoSalud = EstadoSalud.APTO,
                ultimoChequeo = "15/09/2026 - 09:15 hrs"
            )
        )
    )
    val buzos: StateFlow<List<Buzo>> = _buzos.asStateFlow()
    private val _equiposPorBuzo = MutableStateFlow<Map<String, List<EquipoChequeo>>>(emptyMap())
    val equiposPorBuzo: StateFlow<Map<String, List<EquipoChequeo>>> = _equiposPorBuzo.asStateFlow()

    private val _faenas = MutableStateFlow<List<RegistroFaena>>(
        listOf(
            RegistroFaena(
                id = "F-101",
                buzoNombre = "Carlos Soto Paredes",
                empresa = "Servicios Submarinos Austral SpA",
                centroCultivo = "Centro Melinka 1 - Canal Moraleda",
                tipoFaena = "Inspección de redes loberas",
                profundidadMetros = 18,
                tiempoMinutos = 40,
                fechaHora = "15/09/2026 09:00",
                estado = "Completada con Éxito"
            ),
            RegistroFaena(
                id = "F-102",
                buzoNombre = "Rodrigo Mansilla Cárdenas",
                empresa = "Buceos del Sur S.A.",
                centroCultivo = "Centro Chiloé Norte - Isla Lemuy",
                tipoFaena = "Limpieza y retiro de bioincrustaciones en mallas",
                profundidadMetros = 22,
                tiempoMinutos = 35,
                fechaHora = "15/09/2026 10:30",
                estado = "Autorizada y en Curso"
            )
        )
    )
    val faenas: StateFlow<List<RegistroFaena>> = _faenas.asStateFlow()

    // Autenticación
    fun login(rutOCorreo: String, contrasena: String): Result<Prevencionista> {
        val limpio = rutOCorreo.trim()
        val usuario = _usuarios.value.firstOrNull {
            (it.rut.equals(limpio, ignoreCase = true) || it.correo.equals(limpio, ignoreCase = true)) &&
                    it.contrasena == contrasena
        }
        return if (usuario != null) {
            _prevencionistaActivo.value = usuario
            Result.success(usuario)
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas. Verifique su RUT/Correo y contraseña."))
        }
    }

    fun registrarPrevencionista(nuevo: Prevencionista): Result<Prevencionista> {
        val existe = _usuarios.value.any { it.rut.equals(nuevo.rut.trim(), ignoreCase = true) }
        return if (existe) {
            Result.failure(IllegalArgumentException("Ya existe un prevencionista registrado con el RUT ${nuevo.rut}"))
        } else {
            _usuarios.update { it + nuevo }
            _prevencionistaActivo.value = nuevo
            Result.success(nuevo)
        }
    }

    fun logout() {
        _prevencionistaActivo.value = null
    }

    // Buzos
    fun agregarBuzo(buzo: Buzo): Result<Buzo> {
        val existe = _buzos.value.any { it.rut.equals(buzo.rut.trim(), ignoreCase = true) }
        return if (existe) {
            Result.failure(IllegalArgumentException("Ya existe un buzo con el RUT ${buzo.rut}"))
        } else {
            val buzoConId = if (buzo.id.isBlank()) buzo.copy(id = (_buzos.value.size + 1).toString()) else buzo
            _buzos.update { it + buzoConId }
            Result.success(buzoConId)
        }
    }

    fun actualizarSaludBuzo(buzoId: String, nuevoEstado: EstadoSalud, ultimoChequeo: String): Result<Unit> {
        val index = _buzos.value.indexOfFirst { it.id == buzoId }
        return if (index != -1) {
            _buzos.update { lista ->
                lista.map { if (it.id == buzoId) it.copy(estadoSalud = nuevoEstado, ultimoChequeo = ultimoChequeo) else it }
            }
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Buzo no encontrado"))
        }
    }

    // Equipos
    fun obtenerEquiposDefault(): List<EquipoChequeo> {
        return listOf(
            EquipoChequeo("e1", "Traje de buceo y botines", "Estanco, espesor térmico adecuado, sin cortes ni filtraciones"),
            EquipoChequeo("e2", "Máscara / Escafandra de buceo", "Visor transparente con sellado hermético y correa en buen estado"),
            EquipoChequeo("e3", "Botella / Tanque de aire comprimido", "Presión manométrica verificada (mínimo 200 BAR) con prueba hidrostática al día"),
            EquipoChequeo("e4", "Regulador principal y fuente alternativa (Octopus)", "Primera y segunda etapa calibradas, boquillas limpias"),
            EquipoChequeo("e5", "Chaleco compensador de flotabilidad (BCD)", "Válvulas de sobrepresión e inflador directo (Power Inflator) operativos"),
            EquipoChequeo("e6", "Manómetro sumergible / Consola", "Lectura continua de presión visible y libre de condensación"),
            EquipoChequeo("e7", "Profundímetro y computador de inmersión", "Batería certificada, medición de profundidad y tiempos de no-descompresión"),
            EquipoChequeo("e8", "Cuchillo de buceo / Cortalíneas", "Ubicado en zona de fácil acceso para casos de atrapamiento en mallas"),
            EquipoChequeo("e9", "Cabo de vida / Arnés de seguridad", "Cabo guía para comunicación con superficie y prevencionista")
        )
    }

    fun obtenerEquiposParaBuzo(buzoId: String): List<EquipoChequeo> {
        return _equiposPorBuzo.value[buzoId] ?: obtenerEquiposDefault()
    }

    fun guardarEquiposParaBuzo(buzoId: String, equipos: List<EquipoChequeo>) {
        _equiposPorBuzo.update { actual ->
            actual + (buzoId to equipos)
        }
    }

    // Faenas e Inmersiones
    fun autorizarFaena(faena: RegistroFaena): Result<Unit> {
        _faenas.update { listOf(faena) + it }
        return Result.success(Unit)
    }

    fun completarFaena(faenaId: String): Result<Unit> {
        val index = _faenas.value.indexOfFirst { it.id == faenaId }
        return if (index != -1) {
            _faenas.update { lista ->
                lista.map {
                    if (it.id == faenaId) it.copy(estado = "Completada con Éxito") else it
                }
            }
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Faena no encontrada"))
        }
    }

    fun obtenerFechaHoraActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    companion object {
        @Volatile
        private var INSTANCE: AquaRepository? = null

        fun getInstance(): AquaRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AquaRepository().also { INSTANCE = it }
            }
        }
    }
    // Dentro de AquaRepository
    private val revisiones = mutableMapOf<Triple<String, EtapaComprobacion, String>, RevisionEquipo>()
    private val etapasConfirmadas = mutableSetOf<Pair<String, EtapaComprobacion>>()

    fun obtenerRevisiones(buzoId: String, etapa: EtapaComprobacion): List<RevisionEquipo> =
        revisiones.filterKeys { it.first == buzoId && it.second == etapa }.values.toList()

    fun guardarRevision(buzoId: String, etapa: EtapaComprobacion, revision: RevisionEquipo) {
        revisiones[Triple(buzoId, etapa, revision.equipoId)] = revision
    }

    fun confirmarEtapa(buzoId: String, etapa: EtapaComprobacion) {
        etapasConfirmadas += buzoId to etapa
    }}

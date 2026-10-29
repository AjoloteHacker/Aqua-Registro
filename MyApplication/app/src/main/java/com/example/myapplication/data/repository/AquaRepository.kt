package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.BuzoEntity
import com.example.myapplication.data.local.entity.EmpresaEntity
import com.example.myapplication.data.local.entity.FaenaEntity
import com.example.myapplication.data.model.*
import com.example.myapplication.util.NetworkMonitor
import com.example.myapplication.util.Rutificador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/** Clave de una revisión: (faenaId, etapa, equipoId). */
typealias ClaveRevision = Triple<String, EtapaComprobacion, String>

/** Pantalla a la que se pide llevar la revisión de una faena. */
enum class DestinoRevision { EQUIPOS, SALUD }

/**
 * Pedido de Historial a otra pantalla: "revisa esta faena" (y, en Salud, en qué momento).
 * Cada pantalla solo atiende los pedidos dirigidos a ella.
 */
data class SolicitudRevision(
    val faenaId: String,
    val destino: DestinoRevision,
    val momento: MomentoSalud? = null
)

/**
 * Repositorio central de la aplicación.
 * Orquesta los flujos reactivos (StateFlow) y la persistencia local con Room (SQLite).
 */
class AquaRepository private constructor() {

    private var database: AppDatabase? = null
    private var networkMonitor: NetworkMonitor? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Catálogos
    private val _empresas = MutableStateFlow<List<String>>(
        listOf(
            "Servicios Submarinos Austral SpA",
            "AquaSub Chile Limitada",
            "Buceos del Sur S.A.",
            "Patagonia Diving Services"
        )
    )
    val empresas: List<String> get() = _empresas.value
    val empresasFlow: StateFlow<List<String>> = _empresas.asStateFlow()

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
                rut = "12.345.678-5",
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
                rut = "15.489.231-1",
                nombre = "Carlos Soto Paredes",
                empresa = "Servicios Submarinos Austral SpA",
                matricula = "Buzo Comercial N° 2394",
                telefono = "+56 9 8451 2290",
                estadoSalud = EstadoSalud.APTO,
                ultimoChequeo = "15/09/2026 - 08:30 hrs",
                fotoUrl = "https://randomuser.me/api/portraits/men/32.jpg"
            ),
            Buzo(
                id = "2",
                rut = "17.912.453-K",
                nombre = "Pedro Almonacid Vera",
                empresa = "AquaSub Chile Limitada",
                matricula = "Buzo Básico N° 5812",
                telefono = "+56 9 7123 9940",
                estadoSalud = EstadoSalud.PENDIENTE,
                ultimoChequeo = "Pendiente de faena",
                fotoUrl = "https://randomuser.me/api/portraits/men/44.jpg"
            ),
            Buzo(
                id = "3",
                rut = "16.320.108-9",
                nombre = "Rodrigo Mansilla Cárdenas",
                empresa = "Buceos del Sur S.A.",
                matricula = "Buzo Especialista N° 1042",
                telefono = "+56 9 9234 5512",
                estadoSalud = EstadoSalud.APTO,
                ultimoChequeo = "15/09/2026 - 09:15 hrs",
                fotoUrl = "https://randomuser.me/api/portraits/men/68.jpg"
            )
        )
    )
    val buzos: StateFlow<List<Buzo>> = _buzos.asStateFlow()

    private val _equiposPorBuzo = MutableStateFlow<Map<String, List<EquipoChequeo>>>(emptyMap())
    val equiposPorBuzo: StateFlow<Map<String, List<EquipoChequeo>>> = _equiposPorBuzo.asStateFlow()

    // Revisiones de equipos y etapas confirmadas, POR FAENA (reactivas, para que Historial se actualice solo)
    private val _revisiones = MutableStateFlow<Map<ClaveRevision, RevisionEquipo>>(emptyMap())
    val revisiones: StateFlow<Map<ClaveRevision, RevisionEquipo>> = _revisiones.asStateFlow()

    private val _etapasConfirmadas =
        MutableStateFlow<Set<Pair<String, EtapaComprobacion>>>(emptySet())
    val etapasConfirmadas: StateFlow<Set<Pair<String, EtapaComprobacion>>> =
        _etapasConfirmadas.asStateFlow()

    // Encuestas de salud por faena y momento (ANTES / DESPUÉS)
    private val _encuestas =
        MutableStateFlow<Map<Pair<String, MomentoSalud>, EncuestaSalud>>(
            mapOf(
                ("F-101" to MomentoSalud.ANTES) to EncuestaSalud(
                    faenaId = "F-101",
                    momento = MomentoSalud.ANTES,
                    esApto = true,
                    presionArterial = "118/78",
                    frecuenciaCardiaca = 68,
                    registradaPor = "Cristóbal Loncón C.",
                    registradaEn = "15/09/2026 08:45"
                ),
                ("F-101" to MomentoSalud.DESPUES) to EncuestaSalud(
                    faenaId = "F-101",
                    momento = MomentoSalud.DESPUES,
                    esApto = true,
                    presionArterial = "122/80",
                    frecuenciaCardiaca = 74,
                    registradaPor = "Cristóbal Loncón C.",
                    registradaEn = "15/09/2026 09:50"
                )
            )
        )
    val encuestas: StateFlow<Map<Pair<String, MomentoSalud>, EncuestaSalud>> =
        _encuestas.asStateFlow()

    // Historial de registros médicos detallados (Chequeos biomédicos y antropométricos)
    private val _historialMedico = MutableStateFlow<List<Chequeo>>(
        listOf(
            Chequeo(
                id = "CH-102",
                buzoRut = "15.489.231-1",
                buzoNombre = "Carlos Soto Paredes",
                faenaId = "F-101",
                momento = MomentoSalud.DESPUES.etiqueta,
                fechaHora = "15/09/2026 09:50",
                fecha = LocalDate.now(),
                esApto = true,
                pesoKg = 81.0,
                tallaM = 1.76,
                colesterolTotal = 185,
                hdl = 52,
                ldl = 110,
                trigliceridos = 128,
                hemograma = true,
                glicemiaBasal = 91,
                presionArterial = "122/80",
                frecuenciaCardiaca = 74,
                evaluadoPor = "Cristóbal Loncón C. (SEREMI: PR-10892-X)"
            ),
            Chequeo(
                id = "CH-101",
                buzoRut = "15.489.231-1",
                buzoNombre = "Carlos Soto Paredes",
                faenaId = "F-101",
                momento = MomentoSalud.ANTES.etiqueta,
                fechaHora = "15/09/2026 08:45",
                fecha = LocalDate.now(),
                esApto = true,
                pesoKg = 81.0,
                tallaM = 1.76,
                colesterolTotal = 185,
                hdl = 52,
                ldl = 110,
                trigliceridos = 128,
                hemograma = true,
                glicemiaBasal = 91,
                presionArterial = "118/78",
                frecuenciaCardiaca = 68,
                evaluadoPor = "Cristóbal Loncón C. (SEREMI: PR-10892-X)"
            )
        )
    )
    val historialMedico: StateFlow<List<Chequeo>> = _historialMedico.asStateFlow()

    fun registrarChequeoMedico(chequeo: Chequeo) {
        _historialMedico.update { listOf(chequeo) + it }
    }

    // Pedido "revisa esta faena" (Historial -> Control de Equipos / Encuesta de Salud)
    private val _solicitudRevision = MutableStateFlow<SolicitudRevision?>(null)
    val solicitudRevision: StateFlow<SolicitudRevision?> = _solicitudRevision.asStateFlow()

    fun solicitarRevision(solicitud: SolicitudRevision) {
        _solicitudRevision.value = solicitud
    }

    /** Limpia el pedido solo si estaba dirigido a [destino] (evita pisar el de otra pantalla). */
    fun limpiarSolicitud(destino: DestinoRevision) {
        _solicitudRevision.update { if (it?.destino == destino) null else it }
    }

    private val _faenas = MutableStateFlow<List<RegistroFaena>>(
        listOf(
            RegistroFaena(
                id = "F-101",
                buzoId = "1",
                buzoNombre = "Carlos Soto Paredes",
                empresa = "Servicios Submarinos Austral SpA",
                centroCultivo = "Centro Melinka 1 - Canal Moraleda",
                tipoFaena = "Inspección de redes loberas",
                profundidadMetros = 18,
                tiempoMinutos = 40,
                fechaHora = "15/09/2026 09:00",
                estado = RegistroFaena.ESTADO_COMPLETADA,
                autorizadoPor = "Cristóbal Loncón C. (SEREMI: PR-10892-X)",
                aprobacion = AprobacionDatos(
                    aprobadoPor = "Cristóbal Loncón C.",
                    fechaHora = "15/09/2026 09:40"
                )
            ),
            RegistroFaena(
                id = "F-102",
                buzoId = "3",
                buzoNombre = "Rodrigo Mansilla Cárdenas",
                empresa = "Buceos del Sur S.A.",
                centroCultivo = "Centro Chiloé Norte - Isla Lemuy",
                tipoFaena = "Limpieza y retiro de bioincrustaciones en mallas",
                profundidadMetros = 22,
                tiempoMinutos = 35,
                fechaHora = "15/09/2026 10:30",
                estado = RegistroFaena.ESTADO_EN_CURSO,
                autorizadoPor = "Cristóbal Loncón C. (SEREMI: PR-10892-X)"
            )
        )
    )
    val faenas: StateFlow<List<RegistroFaena>> = _faenas.asStateFlow()

    // Monitoreo de Red
    private val _isNetworkConnected = MutableStateFlow(true)
    val isNetworkConnected: StateFlow<Boolean> = _isNetworkConnected.asStateFlow()

    /**
     * Inicializa Room SQLite y el monitor de conectividad de red nativo.
     */
    fun setupDatabase(context: Context) {
        if (database != null) return
        val db = AppDatabase.getInstance(context)
        database = db
        val netMonitor = NetworkMonitor.getInstance(context)
        networkMonitor = netMonitor

        coroutineScope.launch {
            netMonitor.isConnected.collect { online ->
                _isNetworkConnected.value = online
            }
        }

        coroutineScope.launch {
            val buzoDao = db.buzoDao()
            val faenaDao = db.faenaDao()
            val empresaDao = db.empresaDao()

            // 1. Inicializar empresas
            if (empresaDao.count() == 0) {
                empresaDao.insertAll(_empresas.value.map { EmpresaEntity(it) })
            } else {
                _empresas.value = empresaDao.getAllEmpresas().map { it.nombre }
            }

            // 2. Inicializar buzos
            if (buzoDao.count() == 0) {
                buzoDao.insertAll(_buzos.value.map { BuzoEntity.fromDomain(it) })
            } else {
                _buzos.value = buzoDao.getAllBuzos().map { it.toDomain() }
            }

            // 3. Inicializar faenas
            if (faenaDao.count() == 0) {
                faenaDao.insertAll(_faenas.value.map { FaenaEntity.fromDomain(it) })
            } else {
                _faenas.value = faenaDao.getAllFaenas().map { it.toDomain() }
            }
        }
    }

    // Autenticación
    fun login(rutOCorreo: String, contrasena: String): Result<Prevencionista> {
        val entrada = rutOCorreo.trim()
        val rutNormalizado = Rutificador.limpiar(entrada)
        val usuario = _usuarios.value.firstOrNull {
            (it.correo.equals(entrada, ignoreCase = true) ||
             it.rut.equals(entrada, ignoreCase = true) ||
             (rutNormalizado.isNotBlank() && Rutificador.limpiar(it.rut) == rutNormalizado)) &&
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
        val rutLimpio = Rutificador.limpiar(nuevo.rut)
        val existeRut = _usuarios.value.any { Rutificador.limpiar(it.rut) == rutLimpio }
        if (existeRut) {
            return Result.failure(IllegalArgumentException("Ya existe un prevencionista registrado con el RUT ${nuevo.rut}"))
        }
        val correoLimpio = nuevo.correo.trim()
        val existeCorreo = _usuarios.value.any { it.correo.equals(correoLimpio, ignoreCase = true) }
        if (existeCorreo) {
            return Result.failure(IllegalArgumentException("Ya existe un prevencionista registrado con el correo $correoLimpio"))
        }

        val formateado = nuevo.copy(
            nombre = nuevo.nombre.trim(),
            rut = Rutificador.formatear(nuevo.rut),
            correo = correoLimpio,
            registroSeremi = nuevo.registroSeremi.trim().uppercase()
        )
        _usuarios.update { it + formateado }
        _prevencionistaActivo.value = formateado
        return Result.success(formateado)
    }

    fun existeRutPrevencionista(rut: String): Boolean {
        val rutLimpio = Rutificador.limpiar(rut)
        return _usuarios.value.any { Rutificador.limpiar(it.rut) == rutLimpio }
    }

    fun existeCorreoPrevencionista(correo: String): Boolean {
        val correoLimpio = correo.trim()
        return _usuarios.value.any { it.correo.equals(correoLimpio, ignoreCase = true) }
    }

    fun logout() {
        _prevencionistaActivo.value = null
    }

    // Buzos con persistencia Room
    fun agregarBuzo(buzo: Buzo): Result<Buzo> {
        val rutLimpio = Rutificador.limpiar(buzo.rut)
        val existe = _buzos.value.any { Rutificador.limpiar(it.rut) == rutLimpio }
        if (existe) {
            return Result.failure(IllegalArgumentException("Ya existe un buzo registrado con el RUT ${buzo.rut}"))
        }
        val buzoConId = if (buzo.id.isBlank()) buzo.copy(id = (_buzos.value.size + 1).toString()) else buzo
        val buzoFormateado = buzoConId.copy(
            rut = Rutificador.formatear(buzo.rut),
            nombre = buzo.nombre.trim()
        )
        _buzos.update { it + buzoFormateado }

        // Persistir en Room (SQLite)
        coroutineScope.launch {
            database?.buzoDao()?.insertOrUpdate(BuzoEntity.fromDomain(buzoFormateado))
        }

        return Result.success(buzoFormateado)
    }

    fun actualizarSaludBuzo(buzoId: String, nuevoEstado: EstadoSalud, ultimoChequeo: String): Result<Unit> {
        val index = _buzos.value.indexOfFirst { it.id == buzoId }
        return if (index != -1) {
            _buzos.update { lista ->
                lista.map { if (it.id == buzoId) it.copy(estadoSalud = nuevoEstado, ultimoChequeo = ultimoChequeo) else it }
            }
            val buzoActualizado = _buzos.value.firstOrNull { it.id == buzoId }
            if (buzoActualizado != null) {
                coroutineScope.launch {
                    database?.buzoDao()?.insertOrUpdate(BuzoEntity.fromDomain(buzoActualizado))
                }
            }
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Buzo no encontrado"))
        }
    }

    fun editarBuzo(buzo: Buzo): Result<Buzo> {
        val index = _buzos.value.indexOfFirst { it.id == buzo.id }
        if (index == -1) {
            return Result.failure(NoSuchElementException("Buzo no encontrado"))
        }
        val rutLimpio = Rutificador.limpiar(buzo.rut)
        val rutEnUsoPorOtro = _buzos.value.any { it.id != buzo.id && Rutificador.limpiar(it.rut) == rutLimpio }
        if (rutEnUsoPorOtro) {
            return Result.failure(IllegalArgumentException("Ya existe otro buzo registrado con el RUT ${buzo.rut}"))
        }
        val buzoFormateado = buzo.copy(
            rut = Rutificador.formatear(buzo.rut),
            nombre = buzo.nombre.trim(),
            matricula = buzo.matricula.trim(),
            telefono = buzo.telefono.trim(),
            empresa = buzo.empresa.trim()
        )
        _buzos.update { lista ->
            lista.map { if (it.id == buzo.id) buzoFormateado else it }
        }

        // Persistir en Room (SQLite)
        coroutineScope.launch {
            database?.buzoDao()?.insertOrUpdate(BuzoEntity.fromDomain(buzoFormateado))
        }

        return Result.success(buzoFormateado)
    }

    fun eliminarBuzo(buzoId: String): Result<Unit> {
        val buzo = _buzos.value.firstOrNull { it.id == buzoId }
            ?: return Result.failure(NoSuchElementException("Buzo no encontrado"))

        val tieneFaenaEnCurso = _faenas.value.any {
            (it.buzoId == buzoId || it.buzoNombre.equals(buzo.nombre, ignoreCase = true)) &&
            it.enCurso
        }
        if (tieneFaenaEnCurso) {
            return Result.failure(IllegalStateException("No es posible eliminar a ${buzo.nombre} porque tiene una faena en curso."))
        }

        _buzos.update { lista -> lista.filterNot { it.id == buzoId } }
        _equiposPorBuzo.update { map -> map - buzoId }

        // Eliminar en Room (SQLite)
        coroutineScope.launch {
            database?.buzoDao()?.deleteById(buzoId)
        }

        return Result.success(Unit)
    }

    fun agregarEmpresa(nuevaEmpresa: String): Result<String> {
        val limpia = nuevaEmpresa.trim()
        if (limpia.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre de la empresa no puede estar en blanco."))
        }
        if (_empresas.value.any { it.equals(limpia, ignoreCase = true) }) {
            return Result.success(limpia)
        }
        _empresas.update { it + limpia }

        // Persistir en Room (SQLite)
        coroutineScope.launch {
            database?.empresaDao()?.insert(EmpresaEntity(limpia))
        }

        return Result.success(limpia)
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

    // Revisiones y etapas: cada faena tiene su propio checklist, evidencia y etapas confirmadas
    fun obtenerRevisiones(faenaId: String, etapa: EtapaComprobacion): List<RevisionEquipo> =
        _revisiones.value
            .filterKeys { it.first == faenaId && it.second == etapa }
            .values
            .toList()

    fun guardarRevision(faenaId: String, etapa: EtapaComprobacion, revision: RevisionEquipo) {
        _revisiones.update { actual ->
            actual + (Triple(faenaId, etapa, revision.equipoId) to revision)
        }
    }

    fun confirmarEtapa(faenaId: String, etapa: EtapaComprobacion) {
        _etapasConfirmadas.update { it + (faenaId to etapa) }
    }

    fun etapaConfirmada(faenaId: String, etapa: EtapaComprobacion): Boolean =
        (faenaId to etapa) in _etapasConfirmadas.value

    /** Catálogo de equipos del buzo de la faena + revisiones guardadas de ESA faena. */
    fun revisionesCombinadas(faena: RegistroFaena, etapa: EtapaComprobacion): List<RevisionEquipo> {
        val guardadas = obtenerRevisiones(faena.id, etapa).associateBy { it.equipoId }
        return obtenerEquiposParaBuzo(faena.buzoId).map { eq ->
            guardadas[eq.id] ?: RevisionEquipo(
                equipoId = eq.id,
                categoria = "General",
                nombre = eq.nombre,
                descripcionUso = eq.descripcion
            )
        }
    }

    /** Foto actual de la faena (salud del buzo + revisiones + etapas confirmadas) para evaluarla. */
    fun avanceDe(faena: RegistroFaena): AvanceFaena {
        val etapas = EtapaComprobacion.entries.associateWith { etapa ->
            revisionesCombinadas(faena, etapa)
        }
        val confirmadas = _etapasConfirmadas.value
            .filter { it.first == faena.id }
            .map { it.second }
            .toSet()
        val encuestasDeLaFaena = MomentoSalud.entries
            .mapNotNull { momento ->
                _encuestas.value[faena.id to momento]?.let { momento to it }
            }
            .toMap()
        return AvanceFaena(
            buzo = _buzos.value.firstOrNull { it.id == faena.buzoId },
            etapas = etapas,
            etapasConfirmadas = confirmadas,
            encuestas = encuestasDeLaFaena
        )
    }

    // Faenas e Inmersiones con persistencia Room
    fun autorizarFaena(faena: RegistroFaena): Result<Unit> {
        _faenas.update { listOf(faena) + it }
        coroutineScope.launch {
            database?.faenaDao()?.insertOrUpdate(FaenaEntity.fromDomain(faena))
        }
        return Result.success(Unit)
    }

    /**
     * Finaliza la faena solo si la propia faena lo permite (ver [RegistroFaena.pendientes]).
     * Si falta algo, devuelve un [FaenaIncompletaException] con la lista de pendientes.
     */
    fun completarFaena(faenaId: String): Result<RegistroFaena> {
        val faena = _faenas.value.firstOrNull { it.id == faenaId }
            ?: return Result.failure(NoSuchElementException("Faena no encontrada"))

        return faena.finalizar(avanceDe(faena)).onSuccess { finalizada ->
            _faenas.update { lista ->
                lista.map { if (it.id == faenaId) finalizada else it }
            }
            coroutineScope.launch {
                database?.faenaDao()?.insertOrUpdate(FaenaEntity.fromDomain(finalizada))
            }
        }
    }

    private fun actualizarFaena(
        faenaId: String,
        cambio: (RegistroFaena) -> Result<RegistroFaena>
    ): Result<RegistroFaena> {
        val faena = _faenas.value.firstOrNull { it.id == faenaId }
            ?: return Result.failure(NoSuchElementException("Faena no encontrada"))
        return cambio(faena).onSuccess { nueva ->
            _faenas.update { lista -> lista.map { if (it.id == faenaId) nueva else it } }
            coroutineScope.launch {
                database?.faenaDao()?.insertOrUpdate(FaenaEntity.fromDomain(nueva))
            }
        }
    }

    private fun nombrePrevencionistaActivo(): String =
        _prevencionistaActivo.value?.nombre ?: "Prevencionista"

    /** Aprueba que los datos de la faena son correctos (queda registrado quién y cuándo). */
    fun aprobarDatosFaena(faenaId: String): Result<RegistroFaena> =
        actualizarFaena(faenaId) { it.aprobar(nombrePrevencionistaActivo(), obtenerFechaHoraActual()) }

    fun revocarAprobacionFaena(faenaId: String): Result<RegistroFaena> =
        actualizarFaena(faenaId) { it.revocarAprobacion() }

    /** Corrige los datos de la faena; si algo cambia, la aprobación se anula. */
    fun editarDatosFaena(
        faenaId: String,
        centro: String,
        tipo: String,
        profundidad: Int?,
        minutos: Int?
    ): Result<RegistroFaena> =
        actualizarFaena(faenaId) { it.conDatos(centro, tipo, profundidad, minutos) }

    /**
     * Registra la encuesta de salud de una faena en el [momento] indicado, solo si la propia
     * faena lo permite (ver [RegistroFaena.bloqueoEncuesta]). Si ya había una, la reemplaza.
     * También deja el último resultado en la ficha del buzo.
     */
    fun registrarEncuestaSalud(
        faenaId: String,
        momento: MomentoSalud,
        esApto: Boolean,
        presionArterial: String,
        frecuenciaCardiaca: Int
    ): Result<EncuestaSalud> {
        val faena = _faenas.value.firstOrNull { it.id == faenaId }
            ?: return Result.failure(NoSuchElementException("Faena no encontrada"))

        val bloqueo = faena.bloqueoEncuesta(momento, avanceDe(faena))
        if (bloqueo != null) {
            return Result.failure(IllegalStateException(bloqueo))
        }

        val encuesta = EncuestaSalud(
            faenaId = faenaId,
            momento = momento,
            esApto = esApto,
            presionArterial = presionArterial,
            frecuenciaCardiaca = frecuenciaCardiaca,
            registradaPor = nombrePrevencionistaActivo(),
            registradaEn = obtenerFechaHoraActual()
        )
        _encuestas.update { it + ((faenaId to momento) to encuesta) }
        actualizarSaludBuzo(
            buzoId = faena.buzoId,
            nuevoEstado = if (esApto) EstadoSalud.APTO else EstadoSalud.NO_APTO,
            ultimoChequeo = "${momento.etiqueta} ($faenaId) - PA: $presionArterial mmHg, Pulso: $frecuenciaCardiaca lpm"
        )
        return Result.success(encuesta)
    }

    fun obtenerFechaHoraActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    companion object {
        @Volatile
        private var INSTANCE: AquaRepository? = null

        fun init(context: Context) {
            val repo = getInstance()
            repo.setupDatabase(context)
        }

        fun getInstance(): AquaRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AquaRepository().also { INSTANCE = it }
            }
        }
    }
}

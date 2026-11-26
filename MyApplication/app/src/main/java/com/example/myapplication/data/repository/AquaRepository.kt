package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.Chequeo
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.BuzoEntity
import com.example.myapplication.data.local.entity.ChequeoEntity
import com.example.myapplication.data.local.entity.EmpresaEntity
import com.example.myapplication.data.local.entity.EncuestaSaludEntity
import com.example.myapplication.data.local.entity.EvidenciaEtapaEntity
import com.example.myapplication.data.local.entity.FaenaEntity
import com.example.myapplication.data.local.entity.RevisionEquipoEntity
import com.example.myapplication.data.model.*
import com.example.myapplication.util.NetworkMonitor
import com.example.myapplication.util.Rutificador
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.dto.*
import com.example.myapplication.util.SyncWorker
import com.example.myapplication.data.local.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/** Clave del estado compartido de un equipo: (faenaId, buzoId, equipoId). */
typealias ClaveRevision = Triple<String, String, String>

/** Clave de la evidencia fotográfica: (faenaId, buzoId, etapa). */
typealias ClaveEvidencia = Triple<String, String, EtapaComprobacion>

/** Clave de una etapa confirmada: (faenaId, buzoId, etapa). */
typealias ClaveEtapa = Triple<String, String, EtapaComprobacion>

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
    private var appContext: Context? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _climaMaritimo = MutableStateFlow<String>("Clima Marítimo: 11.5°C · Viento: 14 km/h · Apto Inmersión")
    val climaMaritimo: StateFlow<String> = _climaMaritimo.asStateFlow()

    private val _syncStatus = MutableStateFlow<String>("Sincronizado con Room SQLite")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    /** IDs de registros (buzos, faenas, chequeos/pre-chequeos) guardados en Room pendientes de sincronizar con Spring Boot. */
    private val _pendientesSincronizarIds = MutableStateFlow<Set<String>>(emptySet())
    val pendientesSincronizarIds: StateFlow<Set<String>> = _pendientesSincronizarIds.asStateFlow()

    /** URL base activa del servidor Spring Boot (por defecto ajoloserver público HTTPS via Tailscale Funnel). */
    private val _serverUrl = MutableStateFlow<String>(RetrofitClient.BASE_URL_AQUACHECK)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    /** Indica si el último ping / sincronización con ajoloserver respondió 200 OK. */
    private val _servidorOnline = MutableStateFlow<Boolean>(false)
    val servidorOnline: StateFlow<Boolean> = _servidorOnline.asStateFlow()

    /** Detalle del último ping al servidor (latencia en ms, fecha/hora y modo). */
    private val _ultimoPingDetalle = MutableStateFlow<String>("Verificando conexión con ajoloserver...")
    val ultimoPingDetalle: StateFlow<String> = _ultimoPingDetalle.asStateFlow()

    /** Si el usuario activó manualmente el Modo Offline para demostración, ignora eventos de reconexión automática. */
    @Volatile
    private var modoOfflineForzado: Boolean = false

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
                contrasena = "admin123",
                rol = RolUsuario.SUPERVISOR
            ),
            Prevencionista(
                nombre = "Matías Oyarzún R.",
                rut = "18.765.432-K",
                correo = "m.oyarzun@aquachile.com",
                registroSeremi = "OP-4021-B",
                centro = "Centro Chiloé Norte - Isla Lemuy",
                contrasena = "operador123",
                fotoUrl = "https://randomuser.me/api/portraits/men/52.jpg",
                rol = RolUsuario.OPERADOR_BUCEO
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
                fotoUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                estatura = 1.78f,
                talla = 42f,
                peso = 79.5f,
                alergias = "Ninguna conocida",
                edad = LocalDate.of(1988, 4, 12),
                testEsfuerzoVigente = true,
                examenPreocupacionalVigente = true,
                puntajeCalbuco = 94,
                certificadoAchsUri = "Certificado_ACHS_Carlos_Soto.pdf",
                fechaVencimientoMatricula = LocalDate.now().plusDays(180)
            ),
            Buzo(
                id = "2",
                rut = "17.912.453-K",
                nombre = "Pedro Almonacid Vera",
                empresa = "AquaSub Chile Limitada",
                matricula = "Buzo Básico N° 5812",
                telefono = "+56 9 7123 9940",
                estadoSalud = EstadoSalud.PENDIENTE,
                ultimoChequeo = "Bloqueo: >45 años sin Test de Esfuerzo",
                fotoUrl = "https://randomuser.me/api/portraits/men/44.jpg",
                estatura = 1.72f,
                talla = 40f,
                peso = 74.0f,
                alergias = "Penicilina",
                edad = LocalDate.of(1979, 5, 14),
                testEsfuerzoVigente = false,
                examenPreocupacionalVigente = true,
                puntajeCalbuco = 65,
                certificadoAchsUri = "Certificado_ACHS_Pedro_Almonacid.pdf",
                fechaVencimientoMatricula = LocalDate.now().plusDays(10)
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
                fotoUrl = "https://randomuser.me/api/portraits/men/68.jpg",
                estatura = 1.81f,
                talla = 44f,
                peso = 85.2f,
                alergias = null,
                edad = LocalDate.of(1984, 1, 19),
                testEsfuerzoVigente = true,
                examenPreocupacionalVigente = true,
                puntajeCalbuco = 88,
                certificadoAchsUri = "Certificado_ACHS_Rodrigo_Mansilla.pdf",
                fechaVencimientoMatricula = LocalDate.now().plusDays(35)
            )
        )
    )
    val buzos: StateFlow<List<Buzo>> = _buzos.asStateFlow()

    private val _equiposPorBuzo = MutableStateFlow<Map<String, List<EquipoChequeo>>>(emptyMap())
    val equiposPorBuzo: StateFlow<Map<String, List<EquipoChequeo>>> = _equiposPorBuzo.asStateFlow()

    // Evaluación individual de equipos (compartida entre etapas) y etapas confirmadas, POR FAENA.
    // Reactivas, para que Historial se actualice solo.
    private val _revisiones = MutableStateFlow<Map<ClaveRevision, RevisionEquipo>>(emptyMap())
    val revisiones: StateFlow<Map<ClaveRevision, RevisionEquipo>> = _revisiones.asStateFlow()

    // Fotos globales por faena y etapa: 1 en Pre-equipación, 2 en Equipado.
    private val _evidenciasEquipos =
        MutableStateFlow<Map<ClaveEvidencia, EvidenciaEquipos>>(emptyMap())
    val evidenciasEquipos: StateFlow<Map<ClaveEvidencia, EvidenciaEquipos>> =
        _evidenciasEquipos.asStateFlow()

    private val _etapasConfirmadas = MutableStateFlow<Set<ClaveEtapa>>(emptySet())
    val etapasConfirmadas: StateFlow<Set<ClaveEtapa>> = _etapasConfirmadas.asStateFlow()

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

    // Bitácoras post-inmersión con control de velocidades 24/9 m/min y alertas históricas (Anexo C EA4 / EA5)
    private val _bitacorasPostInmersion = MutableStateFlow<List<BitacoraPostInmersion>>(
        listOf(
            BitacoraPostInmersion(
                id = "BIT-101",
                faenaId = "F-101",
                buzoId = "1",
                buzoNombre = "Carlos Soto Paredes",
                fechaHora = "15/09/2026 09:45",
                profundidadMetros = 18,
                tiempoFondoMinutos = 40,
                velocidadDescensoMpm = 26.5, // Supera límite de 24.0 m/min -> Alerta histórica Jefe de Centro
                velocidadAscensoMpm = 7.8,   // Normal (<= 9.0 m/min)
                origenDato = "Dispositivo Census",
                observaciones = "Descenso acelerado por corriente marina en Canal Moraleda. Se notifica al Jefe de Centro.",
                registradoPor = "Cristóbal Loncón C. (SEREMI: PR-10892-X)"
            )
        )
    )
    val bitacorasPostInmersion: StateFlow<List<BitacoraPostInmersion>> = _bitacorasPostInmersion.asStateFlow()

    fun registrarBitacoraPostInmersion(
        faenaId: String,
        profundidadMetros: Int?,
        tiempoFondoMinutos: Int?,
        velocidadDescensoMpm: Double?,
        velocidadAscensoMpm: Double?,
        origenDato: String,
        observaciones: String
    ): Result<BitacoraPostInmersion> {
        val faena = _faenas.value.firstOrNull { it.id == faenaId }
            ?: return Result.failure(NoSuchElementException("Faena no encontrada"))
        if (profundidadMetros == null || profundidadMetros <= 0) {
            return Result.failure(IllegalArgumentException("La profundidad debe ser mayor a 0 metros."))
        }
        if (tiempoFondoMinutos == null || tiempoFondoMinutos <= 0) {
            return Result.failure(IllegalArgumentException("El tiempo de fondo debe ser mayor a 0 minutos."))
        }
        if (velocidadDescensoMpm == null || velocidadDescensoMpm <= 0.0) {
            return Result.failure(IllegalArgumentException("Ingrese una velocidad de descenso válida (m/min)."))
        }
        if (velocidadAscensoMpm == null || velocidadAscensoMpm <= 0.0) {
            return Result.failure(IllegalArgumentException("Ingrese una velocidad de ascenso válida (m/min)."))
        }

        val nuevoId = "BIT-${100 + _bitacorasPostInmersion.value.size + 1}"
        val responsable = _prevencionistaActivo.value?.let {
            "${it.nombre} (SEREMI: ${it.registroSeremi})"
        } ?: "Prevencionista de Turno"

        val nueva = BitacoraPostInmersion(
            id = nuevoId,
            faenaId = faena.id,
            buzoId = faena.buzoId,
            buzoNombre = faena.buzoNombre,
            fechaHora = obtenerFechaHoraActual(),
            profundidadMetros = profundidadMetros,
            tiempoFondoMinutos = tiempoFondoMinutos,
            velocidadDescensoMpm = velocidadDescensoMpm,
            velocidadAscensoMpm = velocidadAscensoMpm,
            origenDato = origenDato.ifBlank { "Dispositivo Census / Simulación" },
            observaciones = observaciones.trim(),
            registradoPor = responsable
        )

        // No se borran alertas históricas: se acumulan para trazabilidad
        _bitacorasPostInmersion.update { listOf(nueva) + it }
        registrarCambioPendienteSync(nueva.id)

        coroutineScope.launch {
            runCatching {
                RetrofitClient.aquaApi.registrarBitacora(
                    BitacoraInmersionDto(
                        id = nueva.id,
                        faenaId = nueva.faenaId,
                        buzoId = nueva.buzoId,
                        buzoNombre = nueva.buzoNombre,
                        fechaHora = nueva.fechaHora,
                        profundidadMetros = nueva.profundidadMetros,
                        tiempoFondoMinutos = nueva.tiempoFondoMinutos,
                        velocidadDescensoMpm = nueva.velocidadDescensoMpm,
                        velocidadAscensoMpm = nueva.velocidadAscensoMpm,
                        alertaVelocidadSuperada = nueva.alertaVelocidadSuperada,
                        detalleAlerta = nueva.detalleAlerta,
                        registradoPor = nueva.registradoPor,
                        observaciones = nueva.observaciones
                    )
                )
            }
        }

        return Result.success(nueva)
    }

    fun registrarChequeoMedico(chequeo: Chequeo) {
        _historialMedico.update { listOf(chequeo) + it }
        coroutineScope.launch {
            database?.chequeoDao()?.insertOrUpdate(ChequeoEntity.fromDomain(chequeo))
        }
        registrarCambioPendienteSync(chequeo.id)
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
                socorristaId = "2",
                socorristaNombre = "Pedro Almonacid Vera",
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
        appContext = context.applicationContext
        val db = AppDatabase.getInstance(context)
        database = db
        val netMonitor = NetworkMonitor.getInstance(context)
        networkMonitor = netMonitor

        coroutineScope.launch {
            val buzoDao = db.buzoDao()
            val faenaDao = db.faenaDao()
            val empresaDao = db.empresaDao()
            val chequeoDao = db.chequeoDao()

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

            // 4. Inicializar historial médico
            if (chequeoDao.count() == 0) {
                chequeoDao.insertAll(_historialMedico.value.map { ChequeoEntity.fromDomain(it) })
            } else {
                _historialMedico.value = chequeoDao.getAllChequeos().map { it.toDomain() }
            }

            // 5. Cargar revisiones de equipos, evidencias fotográficas y etapas confirmadas desde Room
            val revisionesGuardadas = faenaDao.getAllRevisionesEquipo()
            if (revisionesGuardadas.isNotEmpty()) {
                _revisiones.value = revisionesGuardadas.associate { entity ->
                    ClaveRevision(entity.faenaId, entity.buzoId, entity.equipoId) to entity.toDomain()
                }
            }

            val evidenciasGuardadas = faenaDao.getAllEvidenciasEtapa()
            if (evidenciasGuardadas.isNotEmpty()) {
                val mapaEvidencias = mutableMapOf<ClaveEvidencia, EvidenciaEquipos>()
                val setConfirmadas = mutableSetOf<ClaveEtapa>()
                evidenciasGuardadas.forEach { entity ->
                    val etapa = runCatching { EtapaComprobacion.valueOf(entity.etapa) }.getOrNull()
                    if (etapa != null) {
                        mapaEvidencias[ClaveEvidencia(entity.faenaId, entity.buzoId, etapa)] = entity.toEvidenciaDomain()
                        if (entity.etapaConfirmada) {
                            setConfirmadas.add(ClaveEtapa(entity.faenaId, entity.buzoId, etapa))
                        }
                    }
                }
                _evidenciasEquipos.value = mapaEvidencias
                _etapasConfirmadas.value = setConfirmadas
            }

            // 6. Cargar encuestas de salud por faena desde Room
            val encuestasGuardadas = faenaDao.getAllEncuestasSalud()
            if (encuestasGuardadas.isNotEmpty()) {
                val cargadas = encuestasGuardadas.associate { entity ->
                    val dom = entity.toDomain()
                    (dom.faenaId to dom.momento) to dom
                }
                _encuestas.update { it + cargadas }
            } else {
                _encuestas.value.values.forEach { enc ->
                    faenaDao.insertOrUpdateEncuestaSalud(EncuestaSaludEntity.fromDomain(enc))
                }
            }

            // 7. Cargar URL configurada del servidor desde SessionDataStore
            runCatching {
                val guardada = SessionDataStore(context.applicationContext).serverUrlFlow.firstOrNull()
                if (!guardada.isNullOrBlank()) {
                    val normalizada = RetrofitClient.actualizarBaseUrl(guardada)
                    _serverUrl.value = normalizada
                }
            }

            // 8. Iniciar monitoreo de red nativo y sincronización inicial
            netMonitor.isConnected.collect { onlineSistema ->
                val online = if (modoOfflineForzado) false else onlineSistema
                _isNetworkConnected.value = online
                if (online) {
                    _syncStatus.value = "En línea · Sincronizando..."
                    appContext?.let { SyncWorker.encolarSincronizacion(it) }
                    probarConexionServidor()
                    sincronizarConBackend()
                    consultarClimaMaritimo(centrosCultivo.first())
                } else {
                    _servidorOnline.value = false
                    val cant = _pendientesSincronizarIds.value.size
                    _syncStatus.value = if (cant > 0) {
                        "Modo offline · Pendiente de sincronizar ($cant)"
                    } else {
                        "Modo offline · Guardando en Room"
                    }
                }
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
        val buzoConId = if (buzo.id.isBlank()) buzo.copy(id = "B-${System.currentTimeMillis()}") else buzo
        val buzoFormateado = buzoConId.copy(
            rut = Rutificador.formatear(buzo.rut),
            nombre = buzo.nombre.trim()
        )
        _buzos.update { it + buzoFormateado }

        // Persistir en Room (SQLite)
        coroutineScope.launch {
            database?.buzoDao()?.insertOrUpdate(BuzoEntity.fromDomain(buzoFormateado))
        }
        registrarCambioPendienteSync(buzoFormateado.id)

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
        registrarCambioPendienteSync(buzoFormateado.id)

        return Result.success(buzoFormateado)
    }

    /** Alterna la advertencia de matrícula (60 a 15 días) entre invasiva y no invasiva (docs/nota_3). */
    fun alternarAlertaNoInvasivaBuzo(buzoId: String) {
        _buzos.update { lista ->
            lista.map { b ->
                if (b.id == buzoId) b.copy(alertaMatriculaNoInvasiva = !b.alertaMatriculaNoInvasiva) else b
            }
        }
        _buzos.value.firstOrNull { it.id == buzoId }?.let { actualizado ->
            coroutineScope.launch {
                database?.buzoDao()?.insertOrUpdate(BuzoEntity.fromDomain(actualizado))
            }
        }
    }

    fun eliminarBuzo(buzoId: String): Result<Unit> {
        val buzo = _buzos.value.firstOrNull { it.id == buzoId }
            ?: return Result.failure(NoSuchElementException("Buzo no encontrado"))

        val tieneFaenaEnCurso = _faenas.value.any { faena ->
            faena.enCurso && (
                faena.buzoId == buzoId ||
                    faena.socorristaId == buzoId ||
                    faena.buzoNombre.equals(buzo.nombre, ignoreCase = true) ||
                    faena.socorristaNombre.equals(buzo.nombre, ignoreCase = true)
                )
        }
        if (tieneFaenaEnCurso) {
            return Result.failure(IllegalStateException("No es posible eliminar a ${buzo.nombre} porque tiene una faena en curso."))
        }

        _buzos.update { lista -> lista.filterNot { it.id == buzoId } }
        _equiposPorBuzo.update { map -> map - buzoId }

        // Eliminar en Room (SQLite) y sincronizar eliminación con ajoloserver vía API REST
        coroutineScope.launch {
            database?.buzoDao()?.deleteById(buzoId)
            try {
                val resp = RetrofitClient.aquaApi.deleteBuzo(buzoId)
                if (!resp.isSuccessful && buzo.rut.isNotBlank()) {
                    RetrofitClient.aquaApi.deleteBuzo(buzo.rut)
                }
            } catch (e: Exception) {
                Log.w("AquaRepository", "No se pudo sincronizar eliminación de buzo $buzoId en servidor: ${e.message}")
            }
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

    // Equipos (Incluye los 4 Equipos Críticos exigidos con Línea Base en Anexo B EA4/EA5 + 5 implementos de inmersión)
    fun obtenerEquiposDefault(): List<EquipoChequeo> {
        return listOf(
            EquipoChequeo("e1", "Maleta de suministro de aire", "Presión principal y reserva verificadas, conexiones estancas (Ítem crítico Línea Base)"),
            EquipoChequeo("e2", "Filtros de compresor de aire", "Cartuchos coalescentes y carbón activo al día, libre de humedad/CO (Ítem crítico Línea Base)"),
            EquipoChequeo("e3", "Aletas de propulsión y botines", "Correas de sujeción firmes, pala íntegra sin fisuras (Ítem crítico Línea Base)"),
            EquipoChequeo("e4", "Dispositivo de control Census", "Telemetría digital sincronizada para profundidad y velocidades 24/9 m/min (Ítem crítico Línea Base)"),
            EquipoChequeo("e5", "Traje de buceo y máscara/escafandra", "Estanco, espesor térmico adecuado y visor con sellado hermético"),
            EquipoChequeo("e6", "Botella / Tanque de aire comprimido", "Presión manométrica verificada (mínimo 200 BAR) con prueba hidrostática al día"),
            EquipoChequeo("e7", "Regulador principal y Octopus", "Primera y segunda etapa calibradas, boquillas limpias"),
            EquipoChequeo("e8", "Chaleco compensador (BCD) y manómetro", "Válvulas de sobrepresión, inflador directo y lectura sumergible operativos"),
            EquipoChequeo("e9", "Cuchillo cortalíneas y cabo de vida", "Ubicado en zona de fácil acceso para mallas y comunicación con superficie")
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

    // Revisiones y etapas: cada buzo de cada faena tiene su propio checklist, evidencia
    // y etapas confirmadas. La evaluación individual se guarda UNA sola vez por
    // (faena, buzo, equipo) y se comparte entre etapas.
    fun obtenerRevisiones(faenaId: String, buzoId: String): List<RevisionEquipo> =
        _revisiones.value
            .filterKeys { it.first == faenaId && it.second == buzoId }
            .values
            .toList()

    fun guardarRevision(faenaId: String, buzoId: String, revision: RevisionEquipo) {
        _revisiones.update { actual ->
            actual + (ClaveRevision(faenaId, buzoId, revision.equipoId) to revision)
        }
        coroutineScope.launch {
            database?.faenaDao()?.insertOrUpdateRevisionEquipo(
                RevisionEquipoEntity.fromDomain(faenaId, buzoId, revision)
            )
        }
        if (revision.esEquipoCriticoLineaBase && (revision.fotoActualUri != null || revision.tieneEstado)) {
            registrarCambioPendienteSync(faenaId)
            coroutineScope.launch {
                runCatching {
                    RetrofitClient.aquaApi.registrarEvidencia(
                        EvidenciaFotograficaDto(
                            id = "EV-$faenaId-$buzoId-${revision.equipoId}",
                            faenaId = faenaId,
                            tipoEquipo = revision.codigoTipoEquipoBackend,
                            fotoUrl = revision.fotoActualUri ?: "content://aquacheck/inspeccion_${revision.equipoId}.jpg",
                            fotoLineaBaseUrl = revision.fotoLineaBaseRef,
                            fechaHora = obtenerFechaHoraActual(),
                            responsable = nombrePrevencionistaActivo(),
                            coincideLineaBase = revision.coincideConLineaBase,
                            observaciones = revision.observacionLineaBase.ifBlank { revision.descripcionEstado }
                        )
                    )
                }
            }
        }
    }

    fun obtenerEvidencia(
        faenaId: String,
        buzoId: String,
        etapa: EtapaComprobacion
    ): EvidenciaEquipos =
        _evidenciasEquipos.value[ClaveEvidencia(faenaId, buzoId, etapa)] ?: EvidenciaEquipos()

    fun guardarFotoEvidencia(
        faenaId: String,
        buzoId: String,
        etapa: EtapaComprobacion,
        tipo: TipoFotoEquipos,
        uri: String
    ) {
        val clave = ClaveEvidencia(faenaId, buzoId, etapa)
        var nuevaEvidencia = EvidenciaEquipos()
        _evidenciasEquipos.update { actual ->
            val anterior = actual[clave] ?: EvidenciaEquipos()
            nuevaEvidencia = anterior.copy(
                fotos = anterior.fotos + (tipo to uri),
                registradoEn = System.currentTimeMillis()
            )
            actual + (clave to nuevaEvidencia)
        }
        val confirmada = etapaConfirmada(faenaId, buzoId, etapa)
        coroutineScope.launch {
            database?.faenaDao()?.insertOrUpdateEvidenciaEtapa(
                EvidenciaEtapaEntity.fromDomain(faenaId, buzoId, etapa, nuevaEvidencia, confirmada)
            )
        }
        registrarCambioPendienteSync(faenaId)
    }

    fun confirmarEtapa(faenaId: String, buzoId: String, etapa: EtapaComprobacion) {
        _etapasConfirmadas.update { it + ClaveEtapa(faenaId, buzoId, etapa) }
        val evidenciaActual = obtenerEvidencia(faenaId, buzoId, etapa)
        coroutineScope.launch {
            database?.faenaDao()?.insertOrUpdateEvidenciaEtapa(
                EvidenciaEtapaEntity.fromDomain(faenaId, buzoId, etapa, evidenciaActual, true)
            )
        }
        registrarCambioPendienteSync(faenaId)
    }

    fun etapaConfirmada(faenaId: String, buzoId: String, etapa: EtapaComprobacion): Boolean =
        ClaveEtapa(faenaId, buzoId, etapa) in _etapasConfirmadas.value

    /**
     * Catálogo de equipos de ESE buzo + evaluación guardada de ESA faena.
     * La misma evaluación se devuelve para ambas etapas (estado compartido/heredado).
     */
    fun revisionesCombinadas(
        faena: RegistroFaena,
        buzoId: String,
        etapa: EtapaComprobacion
    ): List<RevisionEquipo> {
        val guardadas = obtenerRevisiones(faena.id, buzoId).associateBy { it.equipoId }
        return obtenerEquiposParaBuzo(buzoId).map { eq ->
            val catDefault = if (eq.id in RevisionEquipo.EQUIPOS_CRITICOS_IDS) {
                "Equipos Críticos · Línea Base (Anexo B)"
            } else {
                "Equipamiento Complementario de Inmersión"
            }
            guardadas[eq.id] ?: RevisionEquipo(
                equipoId = eq.id,
                categoria = catDefault,
                nombre = eq.nombre,
                descripcionUso = eq.descripcion
            )
        }
    }

    /** Foto actual de la faena: un avance por buzo + la encuesta única de salud. */
    fun avanceDe(faena: RegistroFaena): AvanceFaena {
        val porBuzo = faena.asignaciones().associate { asignacion ->
            val etapas = EtapaComprobacion.entries.associateWith { etapa ->
                revisionesCombinadas(faena, asignacion.buzoId, etapa)
            }
            val confirmadas = _etapasConfirmadas.value
                .filter { it.first == faena.id && it.second == asignacion.buzoId }
                .map { it.third }
                .toSet()
            val evidencia = EtapaComprobacion.entries.associateWith { etapa ->
                obtenerEvidencia(faena.id, asignacion.buzoId, etapa)
            }
            asignacion.buzoId to AvanceBuzo(
                buzo = _buzos.value.firstOrNull { it.id == asignacion.buzoId },
                etapas = etapas,
                etapasConfirmadas = confirmadas,
                evidencia = evidencia
            )
        }
        val encuestasDeLaFaena = MomentoSalud.entries
            .mapNotNull { momento ->
                _encuestas.value[faena.id to momento]?.let { momento to it }
            }
            .toMap()
        return AvanceFaena(buzos = porBuzo, encuestas = encuestasDeLaFaena)
    }

    // Faenas e Inmersiones con persistencia Room
    fun autorizarFaena(faena: RegistroFaena): Result<Unit> {
        _faenas.update { listOf(faena) + it }
        coroutineScope.launch {
            database?.faenaDao()?.insertOrUpdate(FaenaEntity.fromDomain(faena))
        }
        registrarCambioPendienteSync(faena.id)
        return Result.success(Unit)
    }

    /**
     * Finaliza la faena solo si la propia faena lo permite (ver [RegistroFaena.pendientes]).
     * Si falta algo, devuelve un [FaenaIncompletaException] con la lista de pendientes.
     */
    fun completarFaena(faenaId: String): Result<RegistroFaena> {
        val activo = _prevencionistaActivo.value
        if (activo != null && !activo.esSupervisor) {
            return Result.failure(
                IllegalStateException(
                    "Restricción de Rol: El perfil '${activo.rol.etiqueta}' no está autorizado para cerrar formalmente una faena. Requiere Supervisor."
                )
            )
        }
        val faena = _faenas.value.firstOrNull { it.id == faenaId }
            ?: return Result.failure(NoSuchElementException("Faena no encontrada"))

        return faena.finalizar(avanceDe(faena)).onSuccess { finalizada ->
            _faenas.update { lista ->
                lista.map { if (it.id == faenaId) finalizada else it }
            }
            coroutineScope.launch {
                database?.faenaDao()?.insertOrUpdate(FaenaEntity.fromDomain(finalizada))
            }
            registrarCambioPendienteSync(finalizada.id)
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
            registrarCambioPendienteSync(nueva.id)
        }
    }

    private fun nombrePrevencionistaActivo(): String =
        _prevencionistaActivo.value?.nombre ?: "Prevencionista"

    /** Aprueba que los datos de la faena son correctos (queda registrado quién y cuándo). Solo rol Supervisor. */
    fun aprobarDatosFaena(faenaId: String): Result<RegistroFaena> {
        val activo = _prevencionistaActivo.value
        if (activo != null && !activo.esSupervisor) {
            return Result.failure(
                IllegalStateException(
                    "Restricción de Rol: El perfil '${activo.rol.etiqueta}' no puede aprobar datos de faena. Requiere Supervisor."
                )
            )
        }
        return actualizarFaena(faenaId) { it.aprobar(nombrePrevencionistaActivo(), obtenerFechaHoraActual()) }
    }

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
     * Cierra anticipadamente una faena en curso ("Faena sin terminar") registrando
     * los motivos de selección múltiple (más los marcados en automático), motivos "Otro"
     * y un comentario que puede seguir editándose después del cierre.
     */
    fun cerrarFaenaSinTerminar(
        faenaId: String,
        motivosSeleccionados: Set<MotivoCierreSinTerminar>,
        otrosMotivos: List<String>,
        comentario: String
    ): Result<RegistroFaena> =
        actualizarFaena(faenaId) { faena ->
            faena.cerrarSinTerminar(
                motivosSeleccionados = motivosSeleccionados,
                otrosMotivos = otrosMotivos,
                comentario = comentario,
                cerradoPor = nombrePrevencionistaActivo(),
                fechaHora = obtenerFechaHoraActual(),
                avance = avanceDe(faena)
            )
        }

    /** Permite editar el comentario de una faena cerrada sin terminar incluso después del cierre. */
    fun actualizarComentarioCierreFaena(
        faenaId: String,
        nuevoComentario: String
    ): Result<RegistroFaena> =
        actualizarFaena(faenaId) { it.actualizarComentarioCierre(nuevoComentario) }

    /** Calcula qué motivos de cierre sin terminar se marcan en automático para [faenaId]. */
    fun motivosAutomaticosCierre(faenaId: String): Set<MotivoCierreSinTerminar> {
        val faena = _faenas.value.firstOrNull { it.id == faenaId } ?: return emptySet()
        return faena.motivosAutomaticosCierre(avanceDe(faena))
    }

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
        frecuenciaCardiaca: Int,
        semaforo: ColorSemaforo = if (esApto) ColorSemaforo.VERDE else ColorSemaforo.ROJO,
        motivosBloqueo: List<String> = emptyList(),
        advertencias: List<String> = emptyList(),
        buzoEvaluadoId: String? = null,
        edadBuzo: Int? = null,
        testEsfuerzoVigente: Boolean? = null,
        examenPreocupacionalVigente: Boolean? = null,
        puntajeCalbuco: Int? = null,
        compresorCumple: Boolean = true,
        buzoEmergenciaCumple: Boolean = true
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
            registradaEn = obtenerFechaHoraActual(),
            semaforo = semaforo,
            motivosBloqueo = motivosBloqueo,
            advertencias = advertencias
        )
        _encuestas.update { it + ((faenaId to momento) to encuesta) }
        coroutineScope.launch {
            database?.faenaDao()?.insertOrUpdateEncuestaSalud(
                EncuestaSaludEntity.fromDomain(encuesta)
            )
        }

        val targetBuzoId = buzoEvaluadoId?.takeIf { it.isNotBlank() } ?: faena.buzoId
        if (testEsfuerzoVigente != null || examenPreocupacionalVigente != null || puntajeCalbuco != null || edadBuzo != null) {
            _buzos.update { lista ->
                lista.map { b ->
                    if (b.id == targetBuzoId) {
                        val nuevaFechaNac = if (edadBuzo != null && b.edadAnios != edadBuzo) {
                            LocalDate.now().minusYears(edadBuzo.toLong())
                        } else b.edad
                        b.copy(
                            edad = nuevaFechaNac,
                            testEsfuerzoVigente = testEsfuerzoVigente ?: b.testEsfuerzoVigente,
                            examenPreocupacionalVigente = examenPreocupacionalVigente ?: b.examenPreocupacionalVigente,
                            puntajeCalbuco = puntajeCalbuco ?: b.puntajeCalbuco
                        )
                    } else b
                }
            }
            _buzos.value.firstOrNull { it.id == targetBuzoId }?.let { actualizado ->
                coroutineScope.launch {
                    database?.buzoDao()?.insertOrUpdate(BuzoEntity.fromDomain(actualizado))
                }
            }
        }

        // La encuesta es única por faena, así que el dictamen se refleja en la ficha
        // de los dos buzos asignados (ejecutor y socorrista).
        listOf(faena.buzoId, faena.socorristaId)
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { id ->
                actualizarSaludBuzo(
                    buzoId = id,
                    nuevoEstado = if (esApto) EstadoSalud.APTO else EstadoSalud.NO_APTO,
                    ultimoChequeo = "${momento.etiqueta} ($faenaId · ${semaforo.codigo}) - PA: $presionArterial mmHg, Pulso: $frecuenciaCardiaca lpm"
                )
            }

        // Enviar validación cruzada al motor de semáforo en Spring Boot cuando haya red
        registrarCambioPendienteSync(faena.id)
        val buzoRef = _buzos.value.firstOrNull { it.id == targetBuzoId }
        coroutineScope.launch {
            runCatching {
                val requestDto = ValidacionPreChequeoRequestDto(
                    faenaId = faena.id,
                    buzoId = targetBuzoId,
                    edadBuzo = edadBuzo ?: buzoRef?.edadAnios ?: 35,
                    imc = buzoRef?.imc ?: 24.5,
                    testEsfuerzoVigente = testEsfuerzoVigente ?: buzoRef?.testEsfuerzoVigente ?: true,
                    preocupacionalVigente = examenPreocupacionalVigente ?: buzoRef?.examenPreocupacionalVigente ?: true,
                    encuestaPreventivaOk = esApto,
                    puntajeCalbuco = puntajeCalbuco ?: buzoRef?.puntajeCalbuco ?: 90,
                    compresorCumple = compresorCumple,
                    buzoEmergenciaCumple = buzoEmergenciaCumple,
                    maletaAireCumple = true,
                    filtrosCompresorCumple = true,
                    aletasCumple = true,
                    censusCumple = true,
                    supervisorNombre = nombrePrevencionistaActivo()
                )
                RetrofitClient.aquaApi.evaluarPreChequeo(requestDto)
            }
        }

        return Result.success(encuesta)
    }

    /**
     * Consulta meteorológica marítima de centros acuícolas (Open-Meteo API externa).
     */
    fun consultarClimaMaritimo(centro: String) {
        coroutineScope.launch {
            try {
                val (lat, lon) = when {
                    centro.contains("Melinka", ignoreCase = true) -> -43.89 to -73.75
                    centro.contains("Chiloé", ignoreCase = true) || centro.contains("Lemuy", ignoreCase = true) -> -42.50 to -73.80
                    centro.contains("Calbuco", ignoreCase = true) -> -41.77 to -73.13
                    centro.contains("Aysén", ignoreCase = true) -> -45.40 to -72.70
                    else -> -42.50 to -73.80
                }

                val response = RetrofitClient.climaApi.getClimaMaritimo(latitude = lat, longitude = lon)
                if (response.isSuccessful && response.body()?.current != null) {
                    val current = response.body()!!.current!!
                    val temp = current.temperature?.let { "${it}°C" } ?: "--"
                    val viento = current.windSpeed?.let { "${it} km/h" } ?: "--"
                    val estado = if ((current.windSpeed ?: 0.0) < 28.0) "Apto Inmersión" else "Alerta Viento Fuerte"
                    _climaMaritimo.value = "Clima Marítimo: $temp · Viento: $viento · $estado"
                } else {
                    _climaMaritimo.value = "Clima Marítimo: 11.4°C · Viento: 12 km/h · Operación Normal"
                }
            } catch (e: Exception) {
                _climaMaritimo.value = "Clima Marítimo: 11.4°C · Viento: 12 km/h (Caché Terreno)"
            }
        }
    }

    /**
     * Registra un cambio local en Room como «Pendiente de sincronizar».
     * Si hay conexión activa, intenta sincronizar en segundo plano; si está offline o el servidor
     * no responde, conserva el estado «Pendiente de sincronizar».
     */
    private fun registrarCambioPendienteSync(id: String) {
        _pendientesSincronizarIds.update { it + id }
        val cant = _pendientesSincronizarIds.value.size
        if (!_isNetworkConnected.value) {
            _syncStatus.value = "Modo offline · Pendiente de sincronizar ($cant)"
        } else {
            _syncStatus.value = "Pendiente de sincronizar ($cant)"
            appContext?.let { ctx ->
                SyncWorker.encolarSincronizacion(ctx)
                sincronizarConBackend()
            }
        }
    }

    /**
     * Permite alternar manualmente el modo terreno offline para demostraciones en vivo (EA5 Paso 6)
     * en hardware real sin perder la proyección ni depender de apagar los datos móviles del teléfono.
     */
    fun alternarModoOfflineDemo() {
        val nuevoEstadoOnline = !_isNetworkConnected.value
        modoOfflineForzado = !nuevoEstadoOnline
        _isNetworkConnected.value = nuevoEstadoOnline
        val cant = _pendientesSincronizarIds.value.size
        if (!nuevoEstadoOnline) {
            _servidorOnline.value = false
            _ultimoPingDetalle.value = "Modo Offline activo (Simulación Terreno Room SQLite)"
            _syncStatus.value = if (cant > 0) {
                "Modo offline · Pendiente de sincronizar ($cant)"
            } else {
                "Modo offline · Guardando en Room"
            }
        } else {
            _syncStatus.value = "Reconectado · Sincronizando..."
            appContext?.let { SyncWorker.encolarSincronizacion(it) }
            probarConexionServidor()
            sincronizarConBackend()
        }
    }

    /**
     * Cambia la URL del servidor Spring Boot (ajoloserver público HTTPS, Tailscale o LAN),
     * la persiste en DataStore y verifica inmediatamente la conexión.
     */
    fun cambiarUrlServidor(nuevaUrl: String) {
        val normalizada = RetrofitClient.actualizarBaseUrl(nuevaUrl)
        _serverUrl.value = normalizada
        coroutineScope.launch {
            appContext?.let { SessionDataStore(it).guardarServerUrl(normalizada) }
        }
        if (modoOfflineForzado) {
            modoOfflineForzado = false
            _isNetworkConnected.value = true
        }
        probarConexionServidor()
        sincronizarConBackend()
    }

    /**
     * Realiza un ping real a GET /api/sync/status en ajoloserver midiendo la latencia en ms.
     */
    fun probarConexionServidor() {
        if (!_isNetworkConnected.value) {
            _servidorOnline.value = false
            _ultimoPingDetalle.value = "Modo Offline activo · Datos guardados en Room SQLite"
            return
        }
        coroutineScope.launch {
            _ultimoPingDetalle.value = "Comprobando conexión con el servidor..."
            val inicio = System.currentTimeMillis()
            try {
                val resp = RetrofitClient.aquaApi.getSyncStatus()
                val latenciaMs = (System.currentTimeMillis() - inicio).coerceAtLeast(1L)
                if (resp.isSuccessful) {
                    _servidorOnline.value = true
                    _ultimoPingDetalle.value = "Conectado al servidor (${latenciaMs} ms)"
                } else {
                    _servidorOnline.value = false
                    _ultimoPingDetalle.value = "Respuesta HTTP ${resp.code()} · Operando con Room SQLite"
                }
            } catch (e: Exception) {
                _servidorOnline.value = false
                _ultimoPingDetalle.value = "Sin respuesta del servidor · Operando con Room SQLite"
            }
        }
    }

    @Volatile
    private var ultimoSyncTimestamp: Long = 0L

    /**
     * Refresca los datos en segundo plano desde el servidor si hay conexión activa,
     * limitando las llamadas automáticas a máximo una cada 4 segundos.
     */
    fun refrescarSiEsNecesario() {
        if (!_isNetworkConnected.value || modoOfflineForzado) return
        val ahora = System.currentTimeMillis()
        if (ahora - ultimoSyncTimestamp > 4000L) {
            ultimoSyncTimestamp = ahora
            sincronizarConBackend()
        }
    }

    /**
     * Sincronización bidireccional (POST + GET) entre Room/StateFlow y el backend Spring Boot:
     * 1. Si hay cambios locales pendientes en cola, los envía vía POST /api/sync/batch (solo los pendientes).
     * 2. Descarga buzos, faenas, bitácoras y chequeos para reflejar al instante
     *    cualquier operación ejecutada en el servidor u otros dispositivos.
     */
    fun sincronizarConBackend() {
        if (!_isNetworkConnected.value || modoOfflineForzado) {
            val cant = _pendientesSincronizarIds.value.size
            _syncStatus.value = if (cant > 0) {
                "Modo offline · Pendiente de sincronizar ($cant)"
            } else {
                "Modo offline · Guardando en Room"
            }
            return
        }

        coroutineScope.launch {
            try {
                val pendientesActuales = _pendientesSincronizarIds.value
                if (pendientesActuales.isNotEmpty()) {
                    val buzosDto = _buzos.value
                        .filter { it.id in pendientesActuales }
                        .map { b ->
                            BuzoDto(
                                id = b.id,
                                rut = b.rut,
                                nombre = b.nombre,
                                empresa = b.empresa,
                                matricula = b.matricula,
                                telefono = b.telefono,
                                edad = b.edadAnios,
                                testEsfuerzoVigente = b.testEsfuerzoVigente,
                                examenPreocupacionalVigente = b.examenPreocupacionalVigente,
                                puntajeCalbuco = b.puntajeCalbuco,
                                estadoSalud = b.estadoSalud.name,
                                ultimoChequeo = b.ultimoChequeo,
                                fotoUrl = b.fotoUrl
                            )
                        }

                    val faenasDto = _faenas.value
                        .filter { it.id in pendientesActuales }
                        .map { f ->
                            FaenaDto(
                                id = f.id,
                                buzoId = f.buzoId,
                                buzoNombre = f.buzoNombre,
                                socorristaId = f.socorristaId,
                                socorristaNombre = f.socorristaNombre,
                                empresa = f.empresa,
                                centroCultivo = f.centroCultivo,
                                tipoFaena = f.tipoFaena,
                                profundidadMetros = f.profundidadMetros,
                                tiempoMinutos = f.tiempoMinutos,
                                fechaHora = f.fechaHora,
                                estado = f.estado,
                                autorizadoPor = f.autorizadoPor,
                                aprobadoPor = f.aprobacion?.aprobadoPor,
                                fechaAprobacion = f.aprobacion?.fechaHora
                            )
                        }

                    val bitacorasDto = _bitacorasPostInmersion.value
                        .filter { it.id in pendientesActuales || it.faenaId in pendientesActuales }
                        .map { b ->
                            BitacoraInmersionDto(
                                id = b.id,
                                faenaId = b.faenaId,
                                buzoId = b.buzoId,
                                buzoNombre = b.buzoNombre,
                                fechaHora = b.fechaHora,
                                profundidadMetros = b.profundidadMetros,
                                tiempoFondoMinutos = b.tiempoFondoMinutos,
                                velocidadDescensoMpm = b.velocidadDescensoMpm,
                                velocidadAscensoMpm = b.velocidadAscensoMpm,
                                alertaVelocidadSuperada = b.alertaVelocidadSuperada,
                                detalleAlerta = b.detalleAlerta,
                                registradoPor = b.registradoPor,
                                observaciones = b.observaciones
                            )
                        }

                    val chequeosDto = _historialMedico.value
                        .filter { it.id in pendientesActuales }
                        .map { c ->
                            ChequeoMedicoDto(
                                id = c.id,
                                faenaId = c.faenaId,
                                buzoRut = c.buzoRut,
                                buzoNombre = c.buzoNombre,
                                momento = c.momento,
                                fechaHora = c.fechaHora,
                                esApto = c.esApto,
                                presionArterial = c.presionArterial,
                                frecuenciaCardiaca = c.frecuenciaCardiaca,
                                colesterolTotal = c.colesterolTotal,
                                glicemiaBasal = c.glicemiaBasal,
                                evaluadoPor = c.evaluadoPor
                            )
                        }

                    if (buzosDto.isNotEmpty() || faenasDto.isNotEmpty() || bitacorasDto.isNotEmpty() || chequeosDto.isNotEmpty()) {
                        val respBatch = RetrofitClient.aquaApi.syncBatch(
                            SyncBatchRequestDto(
                                dispositivoOrigenId = "Android-AquaCheck",
                                buzos = buzosDto,
                                faenas = faenasDto,
                                bitacoras = bitacorasDto,
                                chequeosMedicos = chequeosDto
                            )
                        )
                        if (respBatch.isSuccessful && respBatch.body()?.estado == "EXITO") {
                            _pendientesSincronizarIds.update { it - pendientesActuales }
                        }
                    }
                }

                // Descargar cambios realizados desde el servidor (Buzos, Faenas, Bitácoras y Chequeos)
                val respBuzos = RetrofitClient.aquaApi.getBuzos()
                val respFaenas = RetrofitClient.aquaApi.getFaenas()
                val respBitacoras = runCatching { RetrofitClient.aquaApi.getBitacoras() }.getOrNull()
                val respChequeos = runCatching { RetrofitClient.aquaApi.getChequeosMedicos() }.getOrNull()

                if (respBuzos.isSuccessful && !respBuzos.body().isNullOrEmpty()) {
                    val remotos = respBuzos.body()!!
                    val localesPorId = _buzos.value.associateBy { it.id }
                    val buzosCombinados = remotos.mapNotNull { dto ->
                        val id = dto.id ?: return@mapNotNull null
                        val rut = dto.rut ?: return@mapNotNull null
                        val nombre = dto.nombre ?: return@mapNotNull null
                        val local = localesPorId[id]
                        val estadoSalud = runCatching {
                            EstadoSalud.valueOf(dto.estadoSalud ?: "")
                        }.getOrElse { local?.estadoSalud ?: EstadoSalud.PENDIENTE }
                        val fechaNac = when {
                            dto.edad != null && local?.edadAnios != dto.edad ->
                                LocalDate.now().minusYears(dto.edad.toLong())
                            local?.edad != null -> local.edad
                            else -> LocalDate.of(1990, 1, 1)
                        }
                        Buzo(
                            id = id,
                            rut = rut,
                            nombre = nombre,
                            empresa = dto.empresa ?: local?.empresa ?: "Empresa Contratista",
                            matricula = dto.matricula ?: local?.matricula ?: "Buzo Comercial",
                            telefono = dto.telefono ?: local?.telefono ?: "+56 9 9000 0000",
                            estadoSalud = estadoSalud,
                            ultimoChequeo = dto.ultimoChequeo ?: local?.ultimoChequeo ?: "Sincronizado desde Servidor",
                            categoria = local?.categoria ?: "Buzo Comercial",
                            vigenciaMatricula = local?.vigenciaMatricula ?: "Al día",
                            fotoUrl = dto.fotoUrl ?: local?.fotoUrl,
                            estatura = local?.estatura ?: 1.75f,
                            talla = local?.talla ?: 42f,
                            peso = local?.peso ?: 78f,
                            alergias = local?.alergias,
                            edad = fechaNac,
                            testEsfuerzoVigente = dto.testEsfuerzoVigente ?: local?.testEsfuerzoVigente ?: true,
                            examenPreocupacionalVigente = dto.examenPreocupacionalVigente ?: local?.examenPreocupacionalVigente ?: true,
                            puntajeCalbuco = dto.puntajeCalbuco ?: local?.puntajeCalbuco ?: 90,
                            certificadoAchsUri = local?.certificadoAchsUri,
                            fechaVencimientoMatricula = local?.fechaVencimientoMatricula,
                            alertaMatriculaNoInvasiva = local?.alertaMatriculaNoInvasiva ?: false
                        )
                    }
                    val idsRemotos = buzosCombinados.map { it.id }.toSet()
                    val soloLocalesPendientes = _buzos.value.filter { it.id !in idsRemotos && it.id in _pendientesSincronizarIds.value }
                    val listaFinalBuzos = buzosCombinados + soloLocalesPendientes
                    _buzos.value = listaFinalBuzos
                    database?.buzoDao()?.let { dao ->
                        dao.insertAll(listaFinalBuzos.map { BuzoEntity.fromDomain(it) })
                        val eliminados = _buzos.value.filter { it.id !in idsRemotos && it.id !in _pendientesSincronizarIds.value }
                        eliminados.forEach { dao.deleteById(it.id) }
                    }
                }

                if (respFaenas.isSuccessful && !respFaenas.body().isNullOrEmpty()) {
                    val remotos = respFaenas.body()!!
                    val localesPorId = _faenas.value.associateBy { it.id }
                    val faenasCombinadas = remotos.mapNotNull { dto ->
                        val id = dto.id ?: return@mapNotNull null
                        val local = localesPorId[id]
                        val rawEstado = dto.estado.orEmpty()
                        val estadoNormalizado = when {
                            rawEstado.contains("Completada", ignoreCase = true) ->
                                RegistroFaena.ESTADO_COMPLETADA
                            rawEstado.contains("sin Terminar", ignoreCase = true) || rawEstado.contains("Cerrada", ignoreCase = true) ->
                                RegistroFaena.ESTADO_SIN_TERMINAR
                            rawEstado.contains("Curso", ignoreCase = true) ->
                                RegistroFaena.ESTADO_EN_CURSO
                            rawEstado.isNotBlank() -> rawEstado
                            else -> local?.estado ?: RegistroFaena.ESTADO_EN_CURSO
                        }
                        val aprobacion = if (!dto.aprobadoPor.isNullOrBlank()) {
                            AprobacionDatos(
                                aprobadoPor = dto.aprobadoPor,
                                fechaHora = dto.fechaAprobacion ?: obtenerFechaHoraActual()
                            )
                        } else local?.aprobacion

                        val buzoId = dto.buzoId?.ifBlank { null } ?: local?.buzoId ?: "1"
                        val buzoNombre = dto.buzoNombre?.ifBlank { null }
                            ?: local?.buzoNombre
                            ?: _buzos.value.firstOrNull { it.id == buzoId }?.nombre
                            ?: "Buzo"

                        val socorristaId = dto.socorristaId?.ifBlank { null }
                            ?: local?.socorristaId?.ifBlank { null }
                            ?: _buzos.value.firstOrNull { it.id != buzoId }?.id.orEmpty()

                        val socorristaNombre = dto.socorristaNombre?.ifBlank { null }
                            ?: local?.socorristaNombre?.ifBlank { null }
                            ?: _buzos.value.firstOrNull { it.id == socorristaId }?.nombre.orEmpty()

                        RegistroFaena(
                            id = id,
                            buzoId = buzoId,
                            buzoNombre = buzoNombre,
                            socorristaId = socorristaId,
                            socorristaNombre = socorristaNombre,
                            empresa = dto.empresa ?: local?.empresa ?: "Servicios Submarinos Austral SpA",
                            centroCultivo = dto.centroCultivo ?: local?.centroCultivo ?: centrosCultivo.first(),
                            tipoFaena = dto.tipoFaena ?: local?.tipoFaena ?: tiposFaena.first(),
                            profundidadMetros = dto.profundidadMetros ?: local?.profundidadMetros ?: 18,
                            tiempoMinutos = dto.tiempoMinutos ?: local?.tiempoMinutos ?: 40,
                            fechaHora = dto.fechaHora ?: local?.fechaHora ?: obtenerFechaHoraActual(),
                            estado = estadoNormalizado,
                            autorizadoPor = dto.autorizadoPor ?: local?.autorizadoPor ?: "Cristóbal Loncón C.",
                            aprobacion = aprobacion,
                            cierreSinTerminar = local?.cierreSinTerminar
                        )
                    }
                    val idsRemotos = faenasCombinadas.map { it.id }.toSet()
                    val soloLocalesPendientes = _faenas.value.filter { it.id !in idsRemotos && it.id in _pendientesSincronizarIds.value }
                    val listaFinalFaenas = faenasCombinadas + soloLocalesPendientes
                    _faenas.value = listaFinalFaenas
                    database?.faenaDao()?.insertAll(listaFinalFaenas.map { FaenaEntity.fromDomain(it) })
                }

                if (respBitacoras != null && respBitacoras.isSuccessful && !respBitacoras.body().isNullOrEmpty()) {
                    val remotos = respBitacoras.body()!!
                    val localesPorId = _bitacorasPostInmersion.value.associateBy { it.id }
                    val bitacorasCombinadas = remotos.mapNotNull { dto ->
                        val id = dto.id ?: return@mapNotNull null
                        val local = localesPorId[id]
                        BitacoraPostInmersion(
                            id = id,
                            faenaId = dto.faenaId,
                            buzoId = dto.buzoId,
                            buzoNombre = dto.buzoNombre,
                            fechaHora = dto.fechaHora ?: local?.fechaHora ?: obtenerFechaHoraActual(),
                            profundidadMetros = dto.profundidadMetros,
                            tiempoFondoMinutos = dto.tiempoFondoMinutos,
                            velocidadDescensoMpm = dto.velocidadDescensoMpm,
                            velocidadAscensoMpm = dto.velocidadAscensoMpm,
                            origenDato = local?.origenDato ?: "Servidor AquaCheck",
                            observaciones = dto.observaciones ?: local?.observaciones ?: "",
                            registradoPor = dto.registradoPor ?: local?.registradoPor ?: "Prevencionista"
                        )
                    }
                    val idsRemotos = bitacorasCombinadas.map { it.id }.toSet()
                    val soloLocales = _bitacorasPostInmersion.value.filterNot { it.id in idsRemotos }
                    _bitacorasPostInmersion.value = bitacorasCombinadas + soloLocales
                }

                if (respChequeos != null && respChequeos.isSuccessful && !respChequeos.body().isNullOrEmpty()) {
                    val remotos = respChequeos.body()!!
                    val localesPorId = _historialMedico.value.associateBy { it.id }
                    val chequeosCombinados = remotos.mapNotNull { dto ->
                        val id = dto.id ?: return@mapNotNull null
                        val local = localesPorId[id]
                        val buzoRut = dto.buzoRut ?: local?.buzoRut ?: return@mapNotNull null
                        val buzoNombre = dto.buzoNombre ?: local?.buzoNombre ?: "Buzo"
                        val faenaId = dto.faenaId ?: local?.faenaId ?: ""
                        val momento = dto.momento ?: local?.momento ?: "ANTES"
                        val fechaHora = dto.fechaHora ?: local?.fechaHora ?: obtenerFechaHoraActual()
                        val esApto = dto.esApto ?: local?.esApto ?: true
                        val presionArterial = dto.presionArterial ?: local?.presionArterial ?: "120/80"
                        val frecuenciaCardiaca = dto.frecuenciaCardiaca ?: local?.frecuenciaCardiaca ?: 70
                        val colesterol = dto.colesterolTotal ?: local?.colesterolTotal ?: 180
                        val glicemia = dto.glicemiaBasal ?: local?.glicemiaBasal ?: 90
                        val evaluadoPor = dto.evaluadoPor ?: local?.evaluadoPor ?: "Prevencionista"

                        Chequeo(
                            id = id,
                            buzoRut = buzoRut,
                            buzoNombre = buzoNombre,
                            faenaId = faenaId,
                            momento = momento,
                            fechaHora = fechaHora,
                            fecha = local?.fecha ?: LocalDate.now(),
                            esApto = esApto,
                            colesterolTotal = colesterol,
                            hdl = local?.hdl ?: 45,
                            ldl = local?.ldl ?: 110,
                            trigliceridos = local?.trigliceridos ?: 130,
                            hemograma = local?.hemograma ?: true,
                            glicemiaBasal = glicemia,
                            presionArterial = presionArterial,
                            frecuenciaCardiaca = frecuenciaCardiaca,
                            evaluadoPor = evaluadoPor
                        )
                    }
                    val idsRemotos = chequeosCombinados.map { it.id }.toSet()
                    val soloLocales = _historialMedico.value.filterNot { it.id in idsRemotos }
                    val listaFinalChequeos = chequeosCombinados + soloLocales
                    _historialMedico.value = listaFinalChequeos
                    database?.chequeoDao()?.insertAll(listaFinalChequeos.map { ChequeoEntity.fromDomain(it) })
                }

                if (respBuzos.isSuccessful || respFaenas.isSuccessful) {
                    _servidorOnline.value = true
                    val hora = obtenerFechaHoraActual()
                    _syncStatus.value = "Sincronizado ($hora)"
                    _ultimoPingDetalle.value = "Sincronización bidireccional exitosa ($hora)"
                    appContext?.let { SessionDataStore(it).registrarSincronizacionExitosa(hora) }
                } else {
                    _servidorOnline.value = false
                    val cant = _pendientesSincronizarIds.value.size
                    _syncStatus.value = if (cant > 0) {
                        "Guardado en Room · Pendiente de sincronizar ($cant)"
                    } else {
                        "Sincronizado localmente con Room SQLite"
                    }
                }
            } catch (e: Exception) {
                Log.w("AquaRepository", "Error durante sincronización: ${e.message}", e)
                _servidorOnline.value = false
                val cant = _pendientesSincronizarIds.value.size
                _syncStatus.value = if (cant > 0) {
                    "Guardado en Room · Pendiente de sincronizar ($cant)"
                } else {
                    "En línea · Persistencia local Room activa"
                }
            }
        }
    }

    /**
     * Fuerza la sincronización inmediata bidireccional (POST + GET) con Spring Boot y WorkManager.
     */
    fun sincronizarAhora() {
        if (_isNetworkConnected.value && !modoOfflineForzado) {
            _syncStatus.value = "Sincronizando con Spring Boot..."
            appContext?.let { ctx -> SyncWorker.encolarSincronizacion(ctx) }
            sincronizarConBackend()
        } else {
            val cant = _pendientesSincronizarIds.value.size
            _syncStatus.value = if (cant > 0) {
                "Modo offline · Pendiente de sincronizar ($cant)"
            } else {
                "Modo offline · Sin conexión a internet"
            }
        }
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

package com.example.myapplication.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

/**
 * =============================================================================
 * Prevencionista
 * =============================================================================
 * Modelo de datos que representa a un usuario profesional del área de
 * Prevención de Riesgos que opera la aplicación en terreno.
 *
 * @property nombre Nombre completo del profesional.
 * @property rut Documento de identidad (RUT chileno).
 * @property correo Correo institucional (ej: @aquachile.com).
 * @property registroSeremi Número de registro sanitario oficial otorgado por la SEREMI de Salud.
 * @property centro Centro de cultivo o base de operaciones acuícola asignada.
 * @property contrasena Contraseña de acceso al sistema (valor por defecto "admin123" para demo).
 */
data class Prevencionista(
    val nombre: String,
    val rut: String,
    val correo: String,
    val registroSeremi: String,
    val centro: String,
    val contrasena: String = "admin123"
)

/**
 * =============================================================================
 * EstadoSalud
 * =============================================================================
 * Enumeración que tipifica el dictamen biomédico pre-buceo de un trabajador submarino.
 *
 * @property label Texto descriptivo legible para mostrar en etiquetas e insignias de estado.
 */
enum class EstadoSalud(val label: String) {
    APTO("Apto para Inmersión"),
    NO_APTO("No Apto - Precaución"),
    PENDIENTE("Chequeo Pendiente")
}

/**
 * =============================================================================
 * Buzo
 * =============================================================================
 * Entidad que modela a un buzo comercial o profesional que presta servicios en los
 * centros de cultivo acuícola.
 *
 * @property id Identificador único del buzo.
 * @property rut Cédula de identidad (RUT).
 * @property nombre Nombre y apellidos completos del buzo.
 * @property empresa Empresa contratista prestadora de servicios submarinos.
 * @property matricula Matrícula marítima otorgada por la Autoridad Marítima (DIRECTEMAR).
 * @property telefono Teléfono de contacto de emergencia o terreno.
 * @property estadoSalud Estado actual de aptitud médica para sumergirse ([EstadoSalud]).
 * @property ultimoChequeo Fecha y hora del último chequeo biomédico registrado.
 */
data class Buzo(
    val id: String,
    val rut: String,
    val nombre: String,
    val empresa: String,
    val matricula: String,
    val telefono: String,
    var estadoSalud: EstadoSalud = EstadoSalud.PENDIENTE,
    var ultimoChequeo: LocalDate? = null
)

/**
 * =============================================================================
 * EquipoChequeo
 * =============================================================================
 * Modela un implemento técnico individual dentro del checklist de seguridad
 * obligatorio según la normativa de buceo profesional.
 *
 * @property id Código identificador del implemento.
 * @property nombre Denominación técnica del equipo (ej: Tanque 200 BAR, BCD, Octopus).
 * @property descripcion Criterio de inspección y estándar de seguridad exigido.
 * @property verificado Indica si el prevencionista ya inspeccionó y aprobó el implemento.
 * @property obligatorio Si es true, el buzo no puede sumergirse sin este implemento conforme.
 */
data class EquipoChequeo(
    val id: String,
    val nombre: String,
    val descripcion: String,
    var verificado: Boolean = false,
    val obligatorio: Boolean = true
)

/**
 * =============================================================================
 * RegistroFaena
 * =============================================================================
 * Acta digital de autorización e inmersión submarina (Análisis Seguro de Trabajo - AST).
 *
 * @property id Folio o código único de la inmersión (ej: "F-101").
 * @property buzoNombre Nombre del buzo titular asignado a la maniobra.
 * @property empresa Contratista responsable de la faena.
 * @property centroCultivo Centro acuícola donde se ejecuta el trabajo.
 * @property tipoFaena Tipo de maniobra subacuática (mallas, loberas, mortalidad, fondeos).
 * @property profundidadMetros Profundidad planificada en metros bajo el nivel del mar.
 * @property tiempoMinutos Duración planificada de la inmersión en minutos.
 * @property fechaHora Marca temporal del inicio de faena.
 * @property estado Estado operativo ("Autorizada y en Curso", "Completada con Éxito", etc.).
 */
data class RegistroFaena(
    val id: String,
    val buzoNombre: String,
    val empresa: String,
    val centroCultivo: String,
    val tipoFaena: String,
    val profundidadMetros: Int,
    val tiempoMinutos: Int,
    val fechaHora: String,
    val estado: String = "Autorizada y en Curso"
)

/**
 * =============================================================================
 * Chequeo
 * =============================================================================
 * Entidad que modela el registro de chequeo biomédico y evaluación de salud
 * pre-inmersión para un buzo profesional o comercial.
 *
 * @property id Identificador único del registro de chequeo.
 * @property buzoRut Cédula de identidad (RUT) del buzo evaluado.
 * @property fecha Fecha en la que se realizó la evaluación médica.
 * @property esApto Dictamen final de aptitud para el buceo (true si es apto, false de lo contrario).
 * @property pesoKg Peso del buzo en kilogramos.
 * @property tallaM Talla o estatura del buzo en metros.
 * @property colesterolTotal Nivel de colesterol total (mg/dl).
 * @property hdl Nivel de colesterol HDL (mg/dl).
 * @property ldl Nivel de colesterol LDL (mg/dl).
 * @property trigliceridos Nivel de triglicéridos (mg/dl).
 * @property hemograma Indicador del estado del examen de hemograma.
 * @property glicemiaBasal Valor de glicemia basal (mg/dl).
 */
data class Chequeo(
    val id: String,
    val buzoRut: String,
    val fecha: LocalDate,
    val esApto: Boolean,

    val pesoKg: Double,
    val tallaM: Double,
    val colesterolTotal: Int,
    val hdl: Int,
    val ldl: Int,
    val trigliceridos: Int,
    val hemograma: Boolean,
    val glicemiaBasal: Int,
    val presionArterial: String,
    val frecuenciaCardiaca: Int
)

/**
 * =============================================================================
 * AppData
 * =============================================================================
 * Repositorio centralizado en memoria (Singleton) que almacena el estado global
 * de la aplicación durante la sesión de trabajo.
 *
 * Utiliza estructuras reactivas de Compose ([mutableStateListOf] y [mutableStateOf])
 * para que cualquier cambio en los datos se refleje de inmediato en la interfaz de usuario.
 */
object AppData {
    /** Lista reactiva de prevencionistas registrados en el sistema */
    val usuarios = mutableStateListOf(
        Prevencionista(
            nombre = "Cristóbal Loncón C.",
            rut = "12.345.678-9",
            correo = "c.loncon@aquachile.com",
            registroSeremi = "PR-10892-X",
            centro = "Centro Melinka 1 · Fiordo Aisén",
            contrasena = "admin123"
        )
    )

    /** Prevencionista que ha iniciado sesión actualmente en la aplicación */
    var prevencionistaActivo by mutableStateOf(usuarios.first())

    /** Lista reactiva de buzos enrolados con sus exámenes y vigencia marítima */
    val buzos = mutableStateListOf(
        Buzo(
            id = "1",
            rut = "15.489.231-4",
            nombre = "Carlos Soto Paredes",
            empresa = "Servicios Submarinos Austral SpA",
            matricula = "Buzo Comercial N° 2394",
            telefono = "+56 9 8451 2290",
            estadoSalud = EstadoSalud.APTO,
            ultimoChequeo = LocalDate.of(2026, 9, 15)
        ),
        Buzo(
            id = "2",
            rut = "17.912.445-K",
            nombre = "Pedro Almonacid Vera",
            empresa = "AquaSub Chile Limitada",
            matricula = "Buzo Básico N° 5812",
            telefono = "+56 9 7123 9940",
            estadoSalud = EstadoSalud.PENDIENTE,
            ultimoChequeo = null
        ),
        Buzo(
            id = "3",
            rut = "16.320.108-7",
            nombre = "Rodrigo Mansilla Cárdenas",
            empresa = "Buceos del Sur S.A.",
            matricula = "Buzo Especialista N° 1042",
            telefono = "+56 9 9234 5512",
            estadoSalud = EstadoSalud.APTO,
            ultimoChequeo = LocalDate.of(2026, 9, 15)
        )
    )
    val chequeos = mutableStateListOf<Chequeo>()

    /** Catálogo oficial de empresas contratistas de servicios de buceo */
    val empresas = listOf(
        "Servicios Submarinos Austral SpA",
        "AquaSub Chile Limitada",
        "Buceos del Sur S.A.",
        "Patagonia Diving Services"
    )
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.AquaRepository

/**
 * Fachada para compatibilidad con código anterior.
 */
object AppData {
    val repository: AquaRepository get() = AquaRepository.getInstance()

    val empresas: List<String> get() = repository.empresas
    val centrosCultivo: List<String> get() = repository.centrosCultivo
    val tiposFaena: List<String> get() = repository.tiposFaena

    var prevencionistaActivo: Prevencionista
        get() = repository.prevencionistaActivo.value ?: repository.usuarios.value.first()
        set(value) {}

    val buzos: List<Buzo> get() = repository.buzos.value
    val faenas: List<RegistroFaena> get() = repository.faenas.value
    val usuarios: List<Prevencionista> get() = repository.usuarios.value

    fun obtenerEquiposDefault(): List<EquipoChequeo> = repository.obtenerEquiposDefault()
}

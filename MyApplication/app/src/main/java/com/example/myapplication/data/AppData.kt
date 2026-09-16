package com.example.myapplication.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
    var ultimoChequeo: String = "No realizado hoy"
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

    /** Catálogo oficial de empresas contratistas de servicios de buceo */
    val empresas = listOf(
        "Servicios Submarinos Austral SpA",
        "AquaSub Chile Limitada",
        "Buceos del Sur S.A.",
        "Patagonia Diving Services"
    )

    /** Catálogo de centros de cultivo de salmones de AquaChile */
    val centrosCultivo = listOf(
        "Centro Melinka 1 - Canal Moraleda",
        "Centro Chiloé Norte - Isla Lemuy",
        "Centro Aysén 4 - Fiordo Aisén",
        "Centro Calbuco - Isla Huapi"
    )

    /** Tipos estandarizados de faena y maniobra submarina */
    val tiposFaena = listOf(
        "Inspección de redes loberas",
        "Limpieza y retiro de bioincrustaciones en mallas",
        "Extracción segura de mortalidad",
        "Inspección y tensado de fondeos submarinos",
        "Reparación de pasadores y tensores de jaulas"
    )

    /** Bitácora digital centralizada de faenas submarinas */
    val faenas = mutableStateListOf(
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
            estado = "En Faena"
        )
    )

    /**
     * Retorna la lista inicial de los 9 implementos obligatorios que el
     * prevencionista debe auditar en terreno antes de permitir la inmersión.
     */
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
}

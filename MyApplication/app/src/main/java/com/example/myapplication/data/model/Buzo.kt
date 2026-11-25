package com.example.myapplication.data.model

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/**
 * Representa a un buzo registrado en el sistema.
 */
data class Buzo(
    val id: String = "",
    val rut: String = "",
    val nombre: String = "",
    val empresa: String = "",
    val matricula: String = "",
    val telefono: String = "",
    val estadoSalud: EstadoSalud = EstadoSalud.PENDIENTE,
    val ultimoChequeo: String = "No realizado hoy",
    val categoria: String = "Buzo Comercial",
    val vigenciaMatricula: String = "Al día",
    val fotoUrl: String? = null,
    val estatura: Float? = null,
    val talla: Float? = null,
    val peso: Float? = null,
    val alergias: String? = null,
    val edad: LocalDate? = null,
    val testEsfuerzoVigente: Boolean = true,
    val examenPreocupacionalVigente: Boolean = true,
    val puntajeCalbuco: Int = 90,
    val certificadoAchsUri: String? = null,
    val fechaVencimientoMatricula: LocalDate? = null,
    val alertaMatriculaNoInvasiva: Boolean = false
) {
    /** Edad en años calculada a partir de la fecha de nacimiento [edad]. */
    val edadAnios: Int?
        get() = edad?.let { Period.between(it, LocalDate.now()).years.coerceAtLeast(0) }

    /** Índice de Masa Corporal (IMC = peso / estatura²). */
    val imc: Double?
        get() = if (peso != null && estatura != null && estatura > 0f) {
            (peso / (estatura * estatura)).toDouble()
        } else null

    /** Regla crítica EA4/EA5: buzo mayor de 45 años sin test de esfuerzo vigente. */
    val bloqueadoPorEdadSinTestEsfuerzo: Boolean
        get() = (edadAnios ?: 0) > 45 && !testEsfuerzoVigente

    /** Días restantes hasta el vencimiento de la matrícula marítima DIRECTEMAR. */
    val diasRestantesMatricula: Long
        get() = when {
            fechaVencimientoMatricula != null ->
                ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimientoMatricula)
            vigenciaMatricula.contains("Expirada", ignoreCase = true) -> -5L
            vigenciaMatricula.contains("Por renovar", ignoreCase = true) -> 25L
            else -> 180L
        }

    val matriculaVencida: Boolean
        get() = diasRestantesMatricula < 0L || vigenciaMatricula.contains("Expirada", ignoreCase = true)

    /** Menos de 15 días para vencer: lanza pop-up al iniciar sesión. */
    val matriculaCriticaMenos15Dias: Boolean
        get() = !matriculaVencida && diasRestantesMatricula in 0L..14L

    /** Entre 60 y 15 días para vencer: advertencia invasiva que puede pasarse a no invasiva. */
    val matriculaAdvertencia60a15Dias: Boolean
        get() = !matriculaVencida && diasRestantesMatricula in 15L..60L
}

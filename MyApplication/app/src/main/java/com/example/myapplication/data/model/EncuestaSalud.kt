package com.example.myapplication.data.model

/** Semáforo industrial de validación cruzada (Anexo Técnico EA4 / EA5). */
enum class ColorSemaforo(val codigo: String, val etiqueta: String, val resumen: String) {
    VERDE(
        codigo = "VERDE",
        etiqueta = "SEMÁFORO VERDE · AUTORIZADO",
        resumen = "Requisitos biomédicos, edad/test de esfuerzo y equipos críticos validados sin alertas."
    ),
    AMARILLO(
        codigo = "AMARILLO",
        etiqueta = "SEMÁFORO AMARILLO · EN REVISIÓN",
        resumen = "Condición no crítica pendiente de revisión o con observaciones (no bloquea, requiere supervisión)."
    ),
    ROJO(
        codigo = "ROJO",
        etiqueta = "SEMÁFORO ROJO · BLOQUEO CRÍTICO",
        resumen = "Incumplimiento crítico o falta de antecedente indispensable. Se bloquea cierre y aprobación."
    )
}

/** Momento de la faena en que se aplica la encuesta de salud. */
enum class MomentoSalud(val etiqueta: String) {
    /** Con el buzo ya equipado (etapa «Equipado» confirmada). */
    ANTES("Antes de la faena"),

    /** Cuando todo lo demás de la faena ya se revisó. */
    DESPUES("Después de la faena")
}

/** Resultado de una encuesta de salud y validación de semáforo aplicada en una faena concreta. */
data class EncuestaSalud(
    val faenaId: String,
    val momento: MomentoSalud,
    val esApto: Boolean,
    val presionArterial: String,
    val frecuenciaCardiaca: Int,
    val registradaPor: String,
    val registradaEn: String,
    val semaforo: ColorSemaforo = if (esApto) ColorSemaforo.VERDE else ColorSemaforo.ROJO,
    val motivosBloqueo: List<String> = emptyList(),
    val advertencias: List<String> = emptyList()
)

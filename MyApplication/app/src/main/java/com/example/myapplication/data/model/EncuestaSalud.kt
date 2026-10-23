package com.example.myapplication.data.model

/** Momento de la faena en que se aplica la encuesta de salud. */
enum class MomentoSalud(val etiqueta: String) {
    /** Con el buzo ya equipado (etapa «Equipado» confirmada). */
    ANTES("Antes de la faena"),

    /** Cuando todo lo demás de la faena ya se revisó. */
    DESPUES("Después de la faena")
}

/** Resultado de una encuesta de salud aplicada a un buzo en una faena concreta. */
data class EncuestaSalud(
    val faenaId: String,
    val momento: MomentoSalud,
    val esApto: Boolean,
    val presionArterial: String,
    val frecuenciaCardiaca: Int,
    val registradaPor: String,
    val registradaEn: String
)

package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTO para sincronización y comunicación con el backend Spring Boot

data class BuzoDto(
    @SerializedName("id") val id: String,
    @SerializedName("rut") val rut: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("empresa") val empresa: String?,
    @SerializedName("matricula") val matricula: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("edad") val edad: Int?,
    @SerializedName("testEsfuerzoVigente") val testEsfuerzoVigente: Boolean?,
    @SerializedName("examenPreocupacionalVigente") val examenPreocupacionalVigente: Boolean?,
    @SerializedName("puntajeCalbuco") val puntajeCalbuco: Int?,
    @SerializedName("estadoSalud") val estadoSalud: String?,
    @SerializedName("ultimoChequeo") val ultimoChequeo: String?,
    @SerializedName("fotoUrl") val fotoUrl: String?
)

data class FaenaDto(
    @SerializedName("id") val id: String,
    @SerializedName("buzoId") val buzoId: String,
    @SerializedName("buzoNombre") val buzoNombre: String,
    @SerializedName("empresa") val empresa: String?,
    @SerializedName("centroCultivo") val centroCultivo: String?,
    @SerializedName("tipoFaena") val tipoFaena: String?,
    @SerializedName("profundidadMetros") val profundidadMetros: Int?,
    @SerializedName("tiempoMinutos") val tiempoMinutos: Int?,
    @SerializedName("fechaHora") val fechaHora: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("autorizadoPor") val autorizadoPor: String?,
    @SerializedName("aprobadoPor") val aprobadoPor: String?,
    @SerializedName("fechaAprobacion") val fechaAprobacion: String?
)

data class ValidacionPreChequeoRequestDto(
    @SerializedName("faenaId") val faenaId: String,
    @SerializedName("buzoId") val buzoId: String,
    @SerializedName("edadBuzo") val edadBuzo: Int?,
    @SerializedName("imc") val imc: Double?,
    @SerializedName("testEsfuerzoVigente") val testEsfuerzoVigente: Boolean?,
    @SerializedName("preocupacionalVigente") val preocupacionalVigente: Boolean?,
    @SerializedName("encuestaPreventivaOk") val encuestaPreventivaOk: Boolean?,
    @SerializedName("puntajeCalbuco") val puntajeCalbuco: Int?,
    @SerializedName("compresorCumple") val compresorCumple: Boolean?,
    @SerializedName("buzoEmergenciaCumple") val buzoEmergenciaCumple: Boolean?,
    @SerializedName("maletaAireCumple") val maletaAireCumple: Boolean?,
    @SerializedName("filtrosCompresorCumple") val filtrosCompresorCumple: Boolean?,
    @SerializedName("aletasCumple") val aletasCumple: Boolean?,
    @SerializedName("censusCumple") val censusCumple: Boolean?,
    @SerializedName("supervisorNombre") val supervisorNombre: String?
)

data class ValidacionPreChequeoResponseDto(
    @SerializedName("semaforo") val semaforo: String, // "VERDE", "AMARILLO", "ROJO"
    @SerializedName("puedeAprobar") val puedeAprobar: Boolean,
    @SerializedName("estadoAprobacion") val estadoAprobacion: String,
    @SerializedName("motivosBloqueo") val motivosBloqueo: List<String> = emptyList(),
    @SerializedName("advertencias") val advertencias: List<String> = emptyList(),
    @SerializedName("fechaHoraValidacion") val fechaHoraValidacion: String?
)

data class BitacoraInmersionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("faenaId") val faenaId: String,
    @SerializedName("buzoId") val buzoId: String,
    @SerializedName("buzoNombre") val buzoNombre: String,
    @SerializedName("fechaHora") val fechaHora: String? = null,
    @SerializedName("profundidadMetros") val profundidadMetros: Int,
    @SerializedName("tiempoFondoMinutos") val tiempoFondoMinutos: Int,
    @SerializedName("velocidadDescensoMpm") val velocidadDescensoMpm: Double,
    @SerializedName("velocidadAscensoMpm") val velocidadAscensoMpm: Double,
    @SerializedName("alertaVelocidadSuperada") val alertaVelocidadSuperada: Boolean? = null,
    @SerializedName("detalleAlerta") val detalleAlerta: String? = null,
    @SerializedName("registradoPor") val registradoPor: String? = null,
    @SerializedName("observaciones") val observaciones: String? = null
)

data class SyncBatchRequestDto(
    @SerializedName("dispositivoOrigenId") val dispositivoOrigenId: String,
    @SerializedName("buzos") val buzos: List<BuzoDto> = emptyList(),
    @SerializedName("faenas") val faenas: List<FaenaDto> = emptyList(),
    @SerializedName("bitacoras") val bitacoras: List<BitacoraInmersionDto> = emptyList()
)

data class SyncBatchResponseDto(
    @SerializedName("estado") val estado: String,
    @SerializedName("totalSincronizados") val totalSincronizados: Int,
    @SerializedName("fechaHoraServidor") val fechaHoraServidor: String,
    @SerializedName("mensajes") val mensajes: List<String> = emptyList(),
    @SerializedName("advertencias") val advertencias: List<String> = emptyList()
)

// DTO para API Externa de Clima Marítimo (Open-Meteo)
data class ClimaResponseDto(
    @SerializedName("current") val current: CurrentWeatherDto?
)

data class CurrentWeatherDto(
    @SerializedName("time") val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_direction_10m") val windDirection: Double?,
    @SerializedName("weather_code") val weatherCode: Int?
)

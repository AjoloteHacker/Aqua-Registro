package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTO para sincronización y comunicación con el backend Spring Boot

data class BuzoDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("rut") val rut: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("empresa") val empresa: String? = null,
    @SerializedName("matricula") val matricula: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("edad") val edad: Int? = null,
    @SerializedName("testEsfuerzoVigente") val testEsfuerzoVigente: Boolean? = null,
    @SerializedName("examenPreocupacionalVigente") val examenPreocupacionalVigente: Boolean? = null,
    @SerializedName("puntajeCalbuco") val puntajeCalbuco: Int? = null,
    @SerializedName("estadoSalud") val estadoSalud: String? = null,
    @SerializedName("ultimoChequeo") val ultimoChequeo: String? = null,
    @SerializedName("fotoUrl") val fotoUrl: String? = null
)

data class FaenaDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("buzoId") val buzoId: String? = null,
    @SerializedName("buzoNombre") val buzoNombre: String? = null,
    @SerializedName("socorristaId") val socorristaId: String? = null,
    @SerializedName("socorristaNombre") val socorristaNombre: String? = null,
    @SerializedName("empresa") val empresa: String? = null,
    @SerializedName("centroCultivo") val centroCultivo: String? = null,
    @SerializedName("tipoFaena") val tipoFaena: String? = null,
    @SerializedName("profundidadMetros") val profundidadMetros: Int? = null,
    @SerializedName("tiempoMinutos") val tiempoMinutos: Int? = null,
    @SerializedName("fechaHora") val fechaHora: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("autorizadoPor") val autorizadoPor: String? = null,
    @SerializedName("aprobadoPor") val aprobadoPor: String? = null,
    @SerializedName("fechaAprobacion") val fechaAprobacion: String? = null
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

data class EvidenciaFotograficaDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("faenaId") val faenaId: String,
    @SerializedName("tipoEquipo") val tipoEquipo: String, // "MALETA_AIRE", "FILTROS_COMPRESOR", "ALETAS", "CENSUS"
    @SerializedName("fotoUrl") val fotoUrl: String?,
    @SerializedName("fotoLineaBaseUrl") val fotoLineaBaseUrl: String?,
    @SerializedName("fechaHora") val fechaHora: String? = null,
    @SerializedName("responsable") val responsable: String?,
    @SerializedName("coincideLineaBase") val coincideLineaBase: Boolean?,
    @SerializedName("observaciones") val observaciones: String?
)

data class ChequeoMedicoDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("faenaId") val faenaId: String? = null,
    @SerializedName("buzoRut") val buzoRut: String? = null,
    @SerializedName("buzoNombre") val buzoNombre: String? = null,
    @SerializedName("momento") val momento: String? = null,
    @SerializedName("fechaHora") val fechaHora: String? = null,
    @SerializedName("esApto") val esApto: Boolean? = null,
    @SerializedName("presionArterial") val presionArterial: String? = null,
    @SerializedName("frecuenciaCardiaca") val frecuenciaCardiaca: Int? = null,
    @SerializedName("pesoKg") val pesoKg: Double? = null,
    @SerializedName("tallaM") val tallaM: Double? = null,
    @SerializedName("imc") val imc: Double? = null,
    @SerializedName("colesterolTotal") val colesterolTotal: Int? = null,
    @SerializedName("glicemiaBasal") val glicemiaBasal: Int? = null,
    @SerializedName("evaluadoPor") val evaluadoPor: String? = null,
    @SerializedName("observaciones") val observaciones: String? = null
)

data class SyncBatchRequestDto(
    @SerializedName("dispositivoOrigenId") val dispositivoOrigenId: String,
    @SerializedName("buzos") val buzos: List<BuzoDto> = emptyList(),
    @SerializedName("faenas") val faenas: List<FaenaDto> = emptyList(),
    @SerializedName("bitacoras") val bitacoras: List<BitacoraInmersionDto> = emptyList(),
    @SerializedName("evidencias") val evidencias: List<EvidenciaFotograficaDto> = emptyList(),
    @SerializedName("chequeosMedicos") val chequeosMedicos: List<ChequeoMedicoDto> = emptyList()
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

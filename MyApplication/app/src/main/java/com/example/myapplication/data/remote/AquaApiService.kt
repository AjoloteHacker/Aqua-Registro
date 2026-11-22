package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AquaApiService {

    @GET("buzos")
    suspend fun getBuzos(): Response<List<BuzoDto>>

    @POST("buzos")
    suspend fun createOrUpdateBuzo(@Body buzo: BuzoDto): Response<BuzoDto>

    @GET("faenas")
    suspend fun getFaenas(): Response<List<FaenaDto>>

    @POST("faenas")
    suspend fun createFaena(@Body faena: FaenaDto): Response<FaenaDto>

    @PUT("faenas/{id}/aprobar")
    suspend fun aprobarFaena(
        @Path("id") id: String,
        @Query("supervisor") supervisor: String
    ): Response<FaenaDto>

    @POST("pre-chequeos/evaluar")
    suspend fun evaluarPreChequeo(
        @Body request: ValidacionPreChequeoRequestDto
    ): Response<ValidacionPreChequeoResponseDto>

    @POST("bitacoras")
    suspend fun registrarBitacora(
        @Body bitacora: BitacoraInmersionDto
    ): Response<BitacoraInmersionDto>

    @GET("bitacoras/alertas")
    suspend fun getAlertasBitacora(): Response<List<BitacoraInmersionDto>>

    @POST("sync/batch")
    suspend fun syncBatch(
        @Body request: SyncBatchRequestDto
    ): Response<SyncBatchResponseDto>

    @GET("sync/status")
    suspend fun getSyncStatus(): Response<Map<String, Any>>
}

interface ClimaApiService {

    /**
     * Consulta meteorológica marítima de centros acuícolas en la Región de Los Lagos y Aysén
     * (Viento, temperatura del aire y condiciones generales para faenas submarinas).
     */
    @GET("v1/forecast")
    suspend fun getClimaMaritimo(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,wind_speed_10m,wind_direction_10m,weather_code"
    ): Response<ClimaResponseDto>
}

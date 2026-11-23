package com.example.myapplication.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.SessionDataStore
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.dto.BuzoDto
import com.example.myapplication.data.remote.dto.FaenaDto
import com.example.myapplication.data.remote.dto.SyncBatchRequestDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando trabajo de sincronización en segundo plano con WorkManager...")

        return try {
            val db = AppDatabase.getInstance(appContext)
            val buzosLocales = db.buzoDao().getAllBuzos()
            val faenasLocales = db.faenaDao().getAllFaenas()

            val buzosDto = buzosLocales.map {
                BuzoDto(
                    id = it.id,
                    rut = it.rut,
                    nombre = it.nombre,
                    empresa = it.empresa,
                    matricula = it.matricula,
                    telefono = it.telefono,
                    edad = null,
                    testEsfuerzoVigente = null,
                    examenPreocupacionalVigente = null,
                    puntajeCalbuco = null,
                    estadoSalud = it.estadoSalud,
                    ultimoChequeo = it.ultimoChequeo,
                    fotoUrl = it.fotoUrl
                )
            }

            val faenasDto = faenasLocales.map {
                FaenaDto(
                    id = it.id,
                    buzoId = it.buzoId,
                    buzoNombre = it.buzoNombre,
                    socorristaId = it.socorristaId,
                    socorristaNombre = it.socorristaNombre,
                    empresa = it.empresa,
                    centroCultivo = it.centroCultivo,
                    tipoFaena = it.tipoFaena,
                    profundidadMetros = it.profundidadMetros,
                    tiempoMinutos = it.tiempoMinutos,
                    fechaHora = it.fechaHora,
                    estado = it.estado,
                    autorizadoPor = it.autorizadoPor,
                    aprobadoPor = it.aprobadoPor,
                    fechaAprobacion = it.aprobadoEn
                )
            }

            val request = SyncBatchRequestDto(
                dispositivoOrigenId = android.os.Build.MODEL ?: "Android-AquaCheck",
                buzos = buzosDto,
                faenas = faenasDto
            )

            val apiResponse = RetrofitClient.aquaApi.syncBatch(request)

            if (apiResponse.isSuccessful && apiResponse.body()?.estado == "EXITO") {
                val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                Log.d(TAG, "Sincronización completada con éxito: ${apiResponse.body()?.totalSincronizados} registros sincronizados.")
                SessionDataStore(appContext).registrarSincronizacionExitosa(now)
                Result.success()
            } else {
                Log.w(TAG, "Respuesta no exitosa del backend: ${apiResponse.code()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en SyncWorker: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "AquaSyncWorker"
        const val WORK_NAME = "aqua_check_sync_work"

        fun encolarSincronizacion(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }
}

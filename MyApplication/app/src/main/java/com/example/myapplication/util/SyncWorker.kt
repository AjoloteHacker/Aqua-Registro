package com.example.myapplication.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.myapplication.data.local.SessionDataStore
import com.example.myapplication.data.repository.AquaRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando trabajo de sincronización bidireccional en segundo plano con WorkManager...")

        return try {
            val repository = AquaRepository.getInstance()
            repository.setupDatabase(appContext)
            repository.sincronizarConBackend()

            val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            SessionDataStore(appContext).registrarSincronizacionExitosa(now)
            Result.success()
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

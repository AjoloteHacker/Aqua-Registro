package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.repository.AquaRepository

/**
 * Clase Application para inicializar la persistencia Room y servicios globales de la app.
 */
class AquaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AquaRepository.init(this)
    }
}

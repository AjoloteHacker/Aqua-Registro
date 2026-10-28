package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.BuzoDao
import com.example.myapplication.data.local.dao.EmpresaDao
import com.example.myapplication.data.local.dao.FaenaDao
import com.example.myapplication.data.local.entity.BuzoEntity
import com.example.myapplication.data.local.entity.EmpresaEntity
import com.example.myapplication.data.local.entity.FaenaEntity

/**
 * Base de datos principal de Room (SQLite) para Aqua-Registro.
 */
@Database(
    entities = [BuzoEntity::class, FaenaEntity::class, EmpresaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buzoDao(): BuzoDao
    abstract fun faenaDao(): FaenaDao
    abstract fun empresaDao(): EmpresaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aqua_registro.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

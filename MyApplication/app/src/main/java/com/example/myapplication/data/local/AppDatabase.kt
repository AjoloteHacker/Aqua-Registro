package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.BuzoDao
import com.example.myapplication.data.local.dao.ChequeoDao
import com.example.myapplication.data.local.dao.EmpresaDao
import com.example.myapplication.data.local.dao.FaenaDao
import com.example.myapplication.data.local.entity.BuzoEntity
import com.example.myapplication.data.local.entity.ChequeoEntity
import com.example.myapplication.data.local.entity.EmpresaEntity
import com.example.myapplication.data.local.entity.FaenaEntity

/**
 * Base de datos principal de Room (SQLite) para Aqua-Registro.
 */
@Database(
    entities = [BuzoEntity::class, FaenaEntity::class, EmpresaEntity::class, ChequeoEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buzoDao(): BuzoDao
    abstract fun faenaDao(): FaenaDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun chequeoDao(): ChequeoDao

    companion object {
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE buzos ADD COLUMN estatura REAL")
                db.execSQL("ALTER TABLE buzos ADD COLUMN talla REAL")
                db.execSQL("ALTER TABLE buzos ADD COLUMN peso REAL")
                db.execSQL("ALTER TABLE buzos ADD COLUMN alergias TEXT")
                db.execSQL("ALTER TABLE buzos ADD COLUMN edadIso TEXT")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aqua_registro.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    // Solo se usa si la BD instalada es anterior a la versión 3 (no hay ruta de migración).
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

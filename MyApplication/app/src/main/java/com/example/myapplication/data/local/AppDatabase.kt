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
    version = 6,
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `chequeos_new` (
                        `id` TEXT NOT NULL,
                        `buzoRut` TEXT NOT NULL,
                        `buzoNombre` TEXT NOT NULL,
                        `faenaId` TEXT NOT NULL,
                        `momento` TEXT NOT NULL,
                        `fechaHora` TEXT NOT NULL,
                        `fechaIso` TEXT NOT NULL,
                        `esApto` INTEGER NOT NULL,
                        `colesterolTotal` INTEGER NOT NULL,
                        `hdl` INTEGER NOT NULL,
                        `ldl` INTEGER NOT NULL,
                        `trigliceridos` INTEGER NOT NULL,
                        `hemograma` INTEGER NOT NULL,
                        `glicemiaBasal` INTEGER NOT NULL,
                        `presionArterial` TEXT NOT NULL,
                        `frecuenciaCardiaca` INTEGER NOT NULL,
                        `evaluadoPor` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `chequeos_new` (
                        `id`, `buzoRut`, `buzoNombre`, `faenaId`, `momento`, `fechaHora`, `fechaIso`, `esApto`,
                        `colesterolTotal`, `hdl`, `ldl`, `trigliceridos`, `hemograma`, `glicemiaBasal`,
                        `presionArterial`, `frecuenciaCardiaca`, `evaluadoPor`
                    )
                    SELECT
                        `id`, `buzoRut`, `buzoNombre`, `faenaId`, `momento`, `fechaHora`, `fechaIso`, `esApto`,
                        `colesterolTotal`, `hdl`, `ldl`, `trigliceridos`, `hemograma`, `glicemiaBasal`,
                        `presionArterial`, `frecuenciaCardiaca`, `evaluadoPor`
                    FROM `chequeos`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `chequeos`")
                db.execSQL("ALTER TABLE `chequeos_new` RENAME TO `chequeos`")
            }
        }

        /** Faenas con dos buzos: ejecutor + socorrista (buzo secundario). */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `faenas` ADD COLUMN `socorristaId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `faenas` ADD COLUMN `socorristaNombre` TEXT NOT NULL DEFAULT ''")
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
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    // Solo se usa si la BD instalada es anterior a la versión 3 (no hay ruta de migración).
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

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
import com.example.myapplication.data.local.entity.EncuestaSaludEntity
import com.example.myapplication.data.local.entity.EvidenciaEtapaEntity
import com.example.myapplication.data.local.entity.FaenaEntity
import com.example.myapplication.data.local.entity.RevisionEquipoEntity

/**
 * Base de datos principal de Room (SQLite) para Aqua-Registro.
 */
@Database(
    entities = [
        BuzoEntity::class,
        FaenaEntity::class,
        EmpresaEntity::class,
        ChequeoEntity::class,
        RevisionEquipoEntity::class,
        EvidenciaEtapaEntity::class,
        EncuestaSaludEntity::class
    ],
    version = 8,
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

        /** Parámetros de validación cruzada de semáforo en ficha de buzo (EA4 / EA5). */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `buzos` ADD COLUMN `testEsfuerzoVigente` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `buzos` ADD COLUMN `examenPreocupacionalVigente` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `buzos` ADD COLUMN `puntajeCalbuco` INTEGER NOT NULL DEFAULT 90")
            }
        }

        /** Persistencia offline en Room de revisiones de equipos, evidencias fotográficas y encuestas de faena. */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `buzos` ADD COLUMN `certificadoAchsUri` TEXT")
                db.execSQL("ALTER TABLE `buzos` ADD COLUMN `fechaVencimientoMatriculaIso` TEXT")
                db.execSQL("ALTER TABLE `buzos` ADD COLUMN `alertaMatriculaNoInvasiva` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `faenas` ADD COLUMN `cierreMotivos` TEXT")
                db.execSQL("ALTER TABLE `faenas` ADD COLUMN `cierreOtros` TEXT")
                db.execSQL("ALTER TABLE `faenas` ADD COLUMN `cierreComentario` TEXT")
                db.execSQL("ALTER TABLE `faenas` ADD COLUMN `cierrePor` TEXT")
                db.execSQL("ALTER TABLE `faenas` ADD COLUMN `cierreFechaHora` TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `revisiones_equipo` (
                        `id` TEXT NOT NULL,
                        `faenaId` TEXT NOT NULL,
                        `buzoId` TEXT NOT NULL,
                        `equipoId` TEXT NOT NULL,
                        `categoria` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `descripcionUso` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        `descripcionEstado` TEXT NOT NULL,
                        `registradoEn` INTEGER,
                        `fotoActualUri` TEXT,
                        `fotoLineaBaseRef` TEXT NOT NULL,
                        `coincideConLineaBase` INTEGER NOT NULL,
                        `observacionLineaBase` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `evidencias_etapa` (
                        `id` TEXT NOT NULL,
                        `faenaId` TEXT NOT NULL,
                        `buzoId` TEXT NOT NULL,
                        `etapa` TEXT NOT NULL,
                        `fotoGeneralUri` TEXT,
                        `fotoFrenteUri` TEXT,
                        `fotoAtrasUri` TEXT,
                        `etapaConfirmada` INTEGER NOT NULL,
                        `registradoEn` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `encuestas_salud` (
                        `id` TEXT NOT NULL,
                        `faenaId` TEXT NOT NULL,
                        `momento` TEXT NOT NULL,
                        `esApto` INTEGER NOT NULL,
                        `presionArterial` TEXT NOT NULL,
                        `frecuenciaCardiaca` INTEGER NOT NULL,
                        `registradaPor` TEXT NOT NULL,
                        `registradaEn` TEXT NOT NULL,
                        `semaforo` TEXT NOT NULL,
                        `motivosBloqueoText` TEXT NOT NULL,
                        `advertenciasText` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
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
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    // Solo se usa si la BD instalada es anterior a la versión 3 (no hay ruta de migración).
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

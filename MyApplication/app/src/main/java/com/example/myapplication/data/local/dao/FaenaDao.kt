package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.EncuestaSaludEntity
import com.example.myapplication.data.local.entity.EvidenciaEtapaEntity
import com.example.myapplication.data.local.entity.FaenaEntity
import com.example.myapplication.data.local.entity.RevisionEquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FaenaDao {
    @Query("SELECT * FROM faenas ORDER BY id DESC")
    fun getAllFaenasFlow(): Flow<List<FaenaEntity>>

    @Query("SELECT * FROM faenas ORDER BY id DESC")
    fun getAllFaenas(): List<FaenaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(faena: FaenaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(faenas: List<FaenaEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM faenas")
    fun count(): Int

    // Revisiones de equipos por faena y buzo
    @Query("SELECT * FROM revisiones_equipo")
    fun getAllRevisionesEquipo(): List<RevisionEquipoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateRevisionEquipo(revision: RevisionEquipoEntity): Long

    // Evidencias fotográficas y etapas confirmadas por faena y buzo
    @Query("SELECT * FROM evidencias_etapa")
    fun getAllEvidenciasEtapa(): List<EvidenciaEtapaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateEvidenciaEtapa(evidencia: EvidenciaEtapaEntity): Long

    // Encuestas de salud por faena y momento (ANTES / DESPUÉS)
    @Query("SELECT * FROM encuestas_salud")
    fun getAllEncuestasSalud(): List<EncuestaSaludEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateEncuestaSalud(encuesta: EncuestaSaludEntity): Long
}

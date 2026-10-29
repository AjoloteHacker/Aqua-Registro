package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.ChequeoEntity

/**
 * DAO de Room para el acceso persistente a los registros médicos (Chequeos).
 */
@Dao
interface ChequeoDao {

    @Query("SELECT * FROM chequeos ORDER BY id DESC")
    fun getAllChequeos(): List<ChequeoEntity>

    @Query("SELECT COUNT(*) FROM chequeos")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(chequeo: ChequeoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(chequeos: List<ChequeoEntity>): List<Long>
}

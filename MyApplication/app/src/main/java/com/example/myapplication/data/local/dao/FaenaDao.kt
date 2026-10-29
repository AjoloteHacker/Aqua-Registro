package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.FaenaEntity
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
}

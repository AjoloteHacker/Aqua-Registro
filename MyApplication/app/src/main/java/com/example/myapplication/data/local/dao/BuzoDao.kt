package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.BuzoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuzoDao {
    @Query("SELECT * FROM buzos ORDER BY id ASC")
    fun getAllBuzosFlow(): Flow<List<BuzoEntity>>

    @Query("SELECT * FROM buzos ORDER BY id ASC")
    fun getAllBuzos(): List<BuzoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(buzo: BuzoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(buzos: List<BuzoEntity>): List<Long>

    @Query("DELETE FROM buzos WHERE id = :buzoId")
    fun deleteById(buzoId: String): Int

    @Query("SELECT COUNT(*) FROM buzos")
    fun count(): Int
}

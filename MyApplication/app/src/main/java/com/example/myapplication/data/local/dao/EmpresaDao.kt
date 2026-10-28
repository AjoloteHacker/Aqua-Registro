package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.EmpresaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresas ORDER BY nombre ASC")
    fun getAllEmpresasFlow(): Flow<List<EmpresaEntity>>

    @Query("SELECT * FROM empresas ORDER BY nombre ASC")
    fun getAllEmpresas(): List<EmpresaEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(empresa: EmpresaEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(empresas: List<EmpresaEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM empresas")
    fun count(): Int
}

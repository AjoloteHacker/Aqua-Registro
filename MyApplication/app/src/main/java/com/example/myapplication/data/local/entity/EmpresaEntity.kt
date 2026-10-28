package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para el catálogo de empresas contratistas en SQLite.
 */
@Entity(tableName = "empresas")
data class EmpresaEntity(
    @PrimaryKey
    val nombre: String
)

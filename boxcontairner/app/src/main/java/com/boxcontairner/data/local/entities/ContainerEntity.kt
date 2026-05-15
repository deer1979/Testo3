package com.boxcontairner.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.boxcontairner.domain.model.Container

/**
 * Entidad Room del contenedor.
 *
 * NOTA v2: la columna se llama "archived" (consistente con el field Kotlin) — antes había
 * inconsistencia con "isArchived" en los queries del DAO que rompía build/runtime.
 * Se agregaron índices en `code` y `archived` porque son los filtros más usados.
 */
@Entity(
    tableName = "containers",
    indices = [
        Index(value = ["code"], unique = false),
        Index(value = ["archived"])
    ]
)
data class ContainerEntity(
    @PrimaryKey val id: String,
    val code: String,
    val type: String,
    val status: String,
    val lastUpdate: Long,
    val updatedBy: String,
    val reeferSerialNumber: String,
    val reeferModel: String,
    val reeferManufacturer: String,
    val reeferYear: String,
    val reeferVariant: String = "",
    val instruccion: String = "",
    val observations: String = "",
    @ColumnInfo(defaultValue = "0")
    val archived: Boolean = false
)

fun ContainerEntity.toDomain() = Container(
    id = id, code = code, type = type, status = status,
    lastUpdate = lastUpdate, updatedBy = updatedBy,
    reeferSerialNumber = reeferSerialNumber, reeferModel = reeferModel,
    reeferManufacturer = reeferManufacturer, reeferYear = reeferYear,
    reeferVariant = reeferVariant, instruccion = instruccion,
    observations = observations, archived = archived
)

fun Container.toEntity() = ContainerEntity(
    id = id, code = code, type = type, status = status,
    lastUpdate = lastUpdate, updatedBy = updatedBy,
    reeferSerialNumber = reeferSerialNumber, reeferModel = reeferModel,
    reeferManufacturer = reeferManufacturer, reeferYear = reeferYear,
    reeferVariant = reeferVariant, instruccion = instruccion,
    observations = observations, archived = archived
)

package com.boxcontairner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boxcontairner.data.local.entities.ContainerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {

    @Query("SELECT * FROM containers WHERE archived = 0 ORDER BY lastUpdate DESC")
    fun getAllContainers(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE id = :id")
    fun getContainerById(id: String): Flow<ContainerEntity?>

    @Query("SELECT * FROM containers WHERE code = :code LIMIT 1")
    suspend fun getContainerByCode(code: String): ContainerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContainer(container: ContainerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContainers(containers: List<ContainerEntity>)

    @Query("UPDATE containers SET status = :status, lastUpdate = :ts, updatedBy = :nick WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, ts: Long, nick: String)

    @Query("UPDATE containers SET archived = 1, lastUpdate = :ts, updatedBy = :nick WHERE id = :id")
    suspend fun archiveContainer(id: String, ts: Long, nick: String)

    @Query("DELETE FROM containers WHERE id = :id")
    suspend fun deleteContainer(id: String)
}

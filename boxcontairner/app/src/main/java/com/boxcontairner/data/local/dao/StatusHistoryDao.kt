package com.boxcontairner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boxcontairner.data.local.entities.StatusHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusHistoryDao {

    @Query("SELECT * FROM status_history WHERE containerId = :containerId ORDER BY timestamp DESC")
    fun getHistoryForContainer(containerId: String): Flow<List<StatusHistoryEntity>>

    @Query("SELECT COUNT(*) FROM status_history WHERE containerId = :containerId")
    suspend fun getHistoryCount(containerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: StatusHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(history: List<StatusHistoryEntity>)
}

package com.boxcontairner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.boxcontairner.data.local.dao.ContainerDao
import com.boxcontairner.data.local.dao.StatusHistoryDao
import com.boxcontairner.data.local.entities.ContainerEntity
import com.boxcontairner.data.local.entities.StatusHistoryEntity

/**
 * v2 (Room v6): se unificó columna "archived" (antes "isArchived" en queries y "archived"
 * en entity → bug). Como hay destructiveMigration habilitado (DatabaseModule), no se
 * pierde nada que no sea caché local sincronizable desde Firestore.
 */
@Database(
    entities = [ContainerEntity::class, StatusHistoryEntity::class],
    version = 6,
    exportSchema = false
)
abstract class BoxContainerDatabase : RoomDatabase() {
    abstract fun containerDao(): ContainerDao
    abstract fun statusHistoryDao(): StatusHistoryDao
}

package com.boxcontairner.di

import android.content.Context
import androidx.room.Room
import com.boxcontairner.data.local.BoxContainerDatabase
import com.boxcontairner.data.local.dao.ContainerDao
import com.boxcontairner.data.local.dao.StatusHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BoxContainerDatabase =
        Room.databaseBuilder(context, BoxContainerDatabase::class.java, "box_container_db")
            // En producción habrá que escribir migraciones reales — ver MIGRATION_NOTES
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideContainerDao(db: BoxContainerDatabase): ContainerDao = db.containerDao()

    @Provides
    fun provideStatusHistoryDao(db: BoxContainerDatabase): StatusHistoryDao = db.statusHistoryDao()
}

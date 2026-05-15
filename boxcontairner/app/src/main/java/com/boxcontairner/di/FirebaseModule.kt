package com.boxcontairner.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de proveedores de Firebase para Hilt.
 *
 * v2:
 * - Eliminado firebase-vertexai (C5): la dep estaba retirada del BoM y el GenerativeModel
 *   no se inyectaba en ningún sitio del proyecto.
 * - Activada persistencia offline de Firestore (C4): la app es de campo (puertos, baja
 *   señal) y necesita la cola de writes nativa del SDK.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val instance = Firebase.firestore
        instance.firestoreSettings = firestoreSettings {
            setLocalCacheSettings(
                persistentCacheSettings {
                    // 100 MB de caché — ajustá según tamaño esperado del catálogo
                    setSizeBytes(100L * 1024 * 1024)
                }
            )
        }
        return instance
    }
}

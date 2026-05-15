package com.boxcontairner.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manejador de bloqueo local por intentos fallidos de login.
 *
 * Política:
 * - 5 intentos fallidos consecutivos → lockout de 5 minutos.
 * - Login exitoso resetea el contador.
 * - Datos guardados en EncryptedSharedPreferences (no en claro en disco).
 */
@Singleton
class AuthLockoutStore @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_WINDOW_MS = 5 * 60 * 1000L  // 5 minutos
        private const val PREFS_NAME = "auth_lockout"
        private const val KEY_ATTEMPTS = "attempts"
        private const val KEY_LAST_FAIL = "last_fail_at"
    }

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Si por algún motivo EncryptedSharedPreferences falla (raro pero posible
        // tras update de Play Services), cae a SharedPreferences normal.
        // El lockout sigue siendo válido como UX defensiva.
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Si está bloqueado, devuelve los ms restantes hasta poder reintentar.
     * Si no está bloqueado, devuelve null.
     */
    fun remainingLockoutMs(): Long? {
        val attempts = prefs.getInt(KEY_ATTEMPTS, 0)
        if (attempts < MAX_ATTEMPTS) return null

        val lastFail = prefs.getLong(KEY_LAST_FAIL, 0L)
        val elapsed = System.currentTimeMillis() - lastFail
        val remaining = LOCKOUT_WINDOW_MS - elapsed
        if (remaining <= 0) {
            // Ventana expirada → reset implícito
            recordSuccess()
            return null
        }
        return remaining
    }

    fun recordFailure() {
        val current = prefs.getInt(KEY_ATTEMPTS, 0)
        prefs.edit()
            .putInt(KEY_ATTEMPTS, current + 1)
            .putLong(KEY_LAST_FAIL, System.currentTimeMillis())
            .apply()
    }

    fun recordSuccess() {
        prefs.edit().clear().apply()
    }
}

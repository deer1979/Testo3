package com.boxcontairner.data.repository

import com.boxcontairner.core.security.AuthLockoutStore
import com.boxcontairner.domain.model.Role
import com.boxcontairner.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val lockoutStore: AuthLockoutStore
) {
    private val emailSuffix = "@boxcontairner.app"
    private val usersCollection = firestore.collection("users")
    private val bootstrapDoc = firestore.collection("_meta").document("bootstrap")

    private val _currentRole = MutableStateFlow(Role.OPERATOR)
    val currentRole: StateFlow<Role> = _currentRole.asStateFlow()

    /** Devuelve los ms restantes de lockout, o null si no hay lockout activo. */
    fun checkLockout(): Long? = lockoutStore.remainingLockoutMs()

    /**
     * Login. Devuelve true si el usuario está activo y autorizado.
     * Lanza excepción si las credenciales fallan o la cuenta no tiene perfil.
     *
     * v2: Removido el auto-bootstrap de SUPER_ADMIN que estaba en login (C2 del informe) —
     * ahora el bootstrap ocurre exclusivamente en `register` con transacción atómica.
     * Login que llega sin perfil es un error explícito.
     */
    suspend fun login(nick: String, pin: String): Boolean {
        lockoutStore.remainingLockoutMs()?.let { ms ->
            throw IllegalStateException("Demasiados intentos. Reintenta en ${(ms / 1000)} segundos.")
        }

        val email = nick.trim().lowercase() + emailSuffix
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pin).await()
            val uid = authResult.user?.uid
                ?: throw IllegalStateException("Login devolvió sin UID")

            val userDoc = usersCollection.document(uid).get().await()
            if (!userDoc.exists()) {
                // Sesión abierta pero sin perfil = cuenta huérfana. Cerrar y avisar.
                firebaseAuth.signOut()
                throw IllegalStateException(
                    "Tu cuenta no tiene perfil. Pedí al administrador que te dé de alta " +
                        "o intentá el registro nuevamente."
                )
            }

            val isActive = userDoc.getBoolean("isActive") == true
            if (isActive) {
                lockoutStore.recordSuccess()
                _currentRole.value = Role.fromStringOrDefault(userDoc.getString("role"))
                return true
            }
            // No activo: no contar como fallo (no es un intento incorrecto)
            return false
        } catch (e: Exception) {
            // Solo fallos de credencial cuentan al lockout, no errores de red
            if (e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                e is com.google.firebase.auth.FirebaseAuthInvalidUserException
            ) {
                lockoutStore.recordFailure()
            }
            throw e
        }
    }

    /**
     * Registro. Devuelve true si es el primer usuario (queda como SUPER_ADMIN activo)
     * o false si ya hay usuarios (queda como OPERATOR pendiente de aprobación).
     *
     * v2: bootstrap del primer SUPER_ADMIN es ATÓMICO via transacción Firestore que
     * crea `_meta/bootstrap` y `users/{uid}` en un solo paso (C2 resuelto).
     * Si Firestore falla, se borra el user de Auth para evitar huérfanos.
     */
    suspend fun register(nick: String, pin: String): Boolean {
        val email = nick.trim().lowercase() + emailSuffix
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pin).await()
        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Registro devolvió sin UID")

        try {
            val isFirstUser = firestore.runTransaction { txn ->
                val bootstrap = txn.get(bootstrapDoc)
                val isFirst = !bootstrap.exists()

                if (isFirst) {
                    // Atómico: si dos clientes corren esta tx simultáneamente, solo uno gana
                    txn.set(
                        bootstrapDoc,
                        mapOf(
                            "bootstrappedAt" to FieldValue.serverTimestamp(),
                            "by" to uid,
                            "nick" to nick.trim()
                        )
                    )
                    txn.set(
                        usersCollection.document(uid),
                        User(uid = uid, nick = nick.trim(), role = Role.SUPER_ADMIN.name, isActive = true)
                    )
                } else {
                    txn.set(
                        usersCollection.document(uid),
                        User(uid = uid, nick = nick.trim(), role = Role.OPERATOR.name, isActive = false)
                    )
                }
                isFirst
            }.await()

            if (isFirstUser) {
                _currentRole.value = Role.SUPER_ADMIN
            }
            return isFirstUser
        } catch (e: Exception) {
            // Firestore falló → revertir Auth para no dejar usuario huérfano
            Timber.e(e, "Firestore falló durante register. Revirtiendo cuenta Auth para uid=$uid")
            runCatching { firebaseAuth.currentUser?.delete()?.await() }
                .onFailure { Timber.e(it, "No se pudo revertir cuenta Auth uid=$uid") }
            throw IllegalStateException(
                "Falló crear el perfil en la base. Intenta nuevamente con conexión estable.",
                e
            )
        }
    }

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun logout() {
        firebaseAuth.signOut()
        _currentRole.value = Role.OPERATOR
    }

    fun getCurrentUserNick(): String? =
        firebaseAuth.currentUser?.email?.removeSuffix(emailSuffix)

    /** Cargar el rol del usuario actual desde Firestore. Llamar al startup de la app. */
    suspend fun refreshCurrentRole() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        try {
            val doc = usersCollection.document(uid).get().await()
            _currentRole.value = Role.fromStringOrDefault(doc.getString("role"))
        } catch (e: Exception) {
            Timber.w(e, "No se pudo refrescar rol del usuario")
        }
    }
}

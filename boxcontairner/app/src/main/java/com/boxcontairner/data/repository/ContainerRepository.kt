package com.boxcontairner.data.repository

import com.boxcontairner.data.local.dao.ContainerDao
import com.boxcontairner.data.local.dao.StatusHistoryDao
import com.boxcontairner.data.local.entities.StatusHistoryEntity
import com.boxcontairner.data.local.entities.toDomain
import com.boxcontairner.data.local.entities.toEntity
import com.boxcontairner.domain.model.Container
import com.boxcontairner.domain.model.ContainerStatus
import com.boxcontairner.domain.model.HistoryAction
import com.boxcontairner.domain.model.StatusHistory
import com.boxcontairner.domain.model.WorkInstruction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContainerRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val containerDao: ContainerDao,
    private val historyDao: StatusHistoryDao,
    private val authRepository: AuthRepository
) {
    companion object { private const val TAG = "ContainerRepo" }

    private val col = firestore.collection("containers")

    // ── Reads ─────────────────────────────────────────────────────────────────

    fun getContainers(): Flow<List<Container>> =
        containerDao.getAllContainers().map { it.map { e -> e.toDomain() } }

    fun getContainer(id: String): Flow<Container?> =
        containerDao.getContainerById(id).map { it?.toDomain() }

    fun getHistory(containerId: String): Flow<List<StatusHistory>> =
        historyDao.getHistoryForContainer(containerId).map { it.map { e -> e.toDomain() } }

    suspend fun getContainerByCode(code: String): Container? =
        containerDao.getContainerByCode(code)?.toDomain()

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun syncContainers() {
        try {
            val snap = col.get().await()
            val remote = snap.toObjects(Container::class.java)
            containerDao.upsertContainers(remote.map { it.toEntity() })
            Timber.d("Sync OK: ${remote.size} containers desde Firestore")
        } catch (e: Exception) {
            // No es crítico: la app sigue con caché local. Pero hay que loguearlo.
            Timber.w(e, "Sync de containers falló (continúa offline)")
        }
    }

    /**
     * Lógica de ingreso de unidad delegada al UseCase (RegisterContainerEntryUseCase).
     * Este método queda como persistencia pura: guardar+sincronizar.
     */
    suspend fun saveContainerWithHistory(
        container: Container,
        historyAction: HistoryAction,
        historyNote: String
    ): String {
        val nick = authRepository.getCurrentUserNick() ?: "desconocido"
        containerDao.upsertContainer(container.toEntity())
        recordHistory(container.id, historyAction, container.status, container.status, nick, historyNote)
        pushContainerToRemote(container)
        return container.id
    }

    suspend fun updateStatus(containerId: String, previousStatus: String, newStatus: String) {
        val nick = authRepository.getCurrentUserNick() ?: "desconocido"
        val ts = System.currentTimeMillis()
        containerDao.updateStatus(containerId, newStatus, ts, nick)
        recordHistory(containerId, HistoryAction.STATUS_CHANGE, previousStatus, newStatus, nick, "")
        pushPartialUpdate(containerId, mapOf("status" to newStatus, "lastUpdate" to ts, "updatedBy" to nick))
    }

    suspend fun saveDetail(container: Container) {
        val nick = authRepository.getCurrentUserNick() ?: "desconocido"
        val updated = container.copy(lastUpdate = System.currentTimeMillis(), updatedBy = nick)
        containerDao.upsertContainer(updated.toEntity())
        recordHistory(
            container.id, HistoryAction.EDIT, container.status, container.status, nick,
            "Datos editados manualmente"
        )
        pushContainerToRemote(updated)
    }

    /** Si tiene historial → archiva. Si no → elimina definitivamente. */
    suspend fun deleteOrArchive(containerId: String): Boolean {
        val nick = authRepository.getCurrentUserNick() ?: "desconocido"
        val ts = System.currentTimeMillis()
        val hasHistory = historyDao.getHistoryCount(containerId) > 0
        return if (hasHistory) {
            containerDao.archiveContainer(containerId, ts, nick)
            recordHistory(
                containerId, HistoryAction.ARCHIVE, "", "", nick,
                "Unidad archivada — datos de auditoría conservados"
            )
            pushPartialUpdate(containerId, mapOf("archived" to true, "lastUpdate" to ts, "updatedBy" to nick))
            true // archivado
        } else {
            containerDao.deleteContainer(containerId)
            deleteRemote(containerId)
            false // eliminado
        }
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /** Crea el siguiente ID nuevo de container (Firestore-style). */
    fun newId(): String = col.document().id

    /** Genera ID para el contenedor; útil al construir un Container nuevo. */
    suspend fun upsertWithRemoteId(container: Container, historyNote: String): String {
        val withId = if (container.id.isBlank()) container.copy(id = newId()) else container
        return saveContainerWithHistory(withId, HistoryAction.ENTRY, historyNote)
    }

    private suspend fun recordHistory(
        containerId: String,
        action: HistoryAction,
        prev: String,
        new: String,
        nick: String,
        notes: String
    ) {
        val entry = StatusHistoryEntity(
            id = UUID.randomUUID().toString(),
            containerId = containerId,
            action = action.name,
            previousStatus = prev,
            newStatus = new,
            changedBy = nick,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        historyDao.insert(entry)
        try {
            col.document(containerId).collection("history").document(entry.id).set(entry).await()
        } catch (e: Exception) {
            Timber.w(e, "Push historial a Firestore falló (queda en caché local) id=${entry.id}")
        }
    }

    private suspend fun pushContainerToRemote(container: Container) {
        try {
            col.document(container.id).set(container).await()
        } catch (e: Exception) {
            Timber.w(e, "Push container a Firestore falló id=${container.id} (queda en caché local)")
        }
    }

    private suspend fun pushPartialUpdate(containerId: String, updates: Map<String, Any>) {
        try {
            col.document(containerId).update(updates).await()
        } catch (e: Exception) {
            Timber.w(e, "Update parcial a Firestore falló id=$containerId")
        }
    }

    private suspend fun deleteRemote(containerId: String) {
        try {
            col.document(containerId).delete().await()
        } catch (e: Exception) {
            Timber.w(e, "Delete remoto falló id=$containerId")
        }
    }
}

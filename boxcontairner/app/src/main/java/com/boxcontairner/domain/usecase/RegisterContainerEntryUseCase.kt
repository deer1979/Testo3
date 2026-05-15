package com.boxcontairner.domain.usecase

import com.boxcontairner.data.repository.AuthRepository
import com.boxcontairner.data.repository.ContainerRepository
import com.boxcontairner.domain.model.Container
import com.boxcontairner.domain.model.ContainerStatus
import com.boxcontairner.domain.model.HistoryAction
import com.boxcontairner.domain.model.WorkInstruction
import javax.inject.Inject

/**
 * UseCase que maneja el flujo de registro de unidad — diferencia entre ingreso nuevo y
 * re-ingreso de una unidad ya conocida (resetea observaciones pero conserva historial).
 *
 * Antes esta lógica estaba en `ContainerRepository.addOrUpdateContainer` (50 líneas).
 * Se extrajo para separar persistencia de reglas de negocio.
 */
class RegisterContainerEntryUseCase @Inject constructor(
    private val containerRepository: ContainerRepository,
    private val authRepository: AuthRepository
) {
    /**
     * @return el ID del contenedor (nuevo o existente).
     */
    suspend operator fun invoke(input: Container): String {
        val nick = authRepository.getCurrentUserNick() ?: "desconocido"
        val now = System.currentTimeMillis()
        val existing = containerRepository.getContainerByCode(input.code)

        return if (existing != null) {
            handleReentry(existing, input, nick, now)
        } else {
            handleNewEntry(input, nick, now)
        }
    }

    /**
     * Re-ingreso: la unidad ya existía. Conservamos historial, refrescamos campos técnicos,
     * limpiamos observaciones y volvemos a estado INSP. Si estaba archivada, se desarchiva.
     */
    private suspend fun handleReentry(
        existing: Container,
        input: Container,
        nick: String,
        now: Long
    ): String {
        val updated = existing.copy(
            reeferSerialNumber = input.reeferSerialNumber.replace(" ", "").trim(),
            reeferModel = input.reeferModel.trim(),
            reeferManufacturer = input.reeferManufacturer.trim(),
            reeferYear = input.reeferYear.trim(),
            reeferVariant = input.reeferVariant.ifBlank { existing.reeferVariant },
            instruccion = input.instruccion.ifBlank { WorkInstruction.NONE.displayLabel },
            observations = "",                                  // Reset por nuevo ingreso
            status = ContainerStatus.INSP.name,                 // Vuelve a inspección
            updatedBy = nick,
            lastUpdate = now,
            archived = false                                    // Desarchivar si correspondía
        )

        containerRepository.saveContainerWithHistory(
            container = updated,
            historyAction = HistoryAction.ENTRY,
            historyNote = "Re-ingreso de unidad detectado"
        )
        return updated.id
    }

    /**
     * Ingreso nuevo: genera ID, normaliza campos y persiste con historial inicial.
     */
    private suspend fun handleNewEntry(
        input: Container,
        nick: String,
        now: Long
    ): String {
        val normalized = input.copy(
            id = containerRepository.newId(),
            reeferSerialNumber = input.reeferSerialNumber.replace(" ", "").trim(),
            instruccion = input.instruccion.ifBlank { WorkInstruction.NONE.displayLabel },
            status = ContainerStatus.INSP.name,
            updatedBy = nick,
            lastUpdate = now,
            archived = false
        )

        containerRepository.saveContainerWithHistory(
            container = normalized,
            historyAction = HistoryAction.ENTRY,
            historyNote = "Registro inicial de unidad"
        )
        return normalized.id
    }
}

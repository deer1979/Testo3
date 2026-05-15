package com.boxcontairner.domain.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de dominio del contenedor.
 *
 * Los campos enum se guardan como String en Firestore/Room (compatibilidad cross-platform y
 * tolerancia a valores nuevos sin breaking change). Para trabajar tipado en código, usar los
 * getters [statusEnum], [instruccionEnum].
 */
data class Container(
    val id: String = "",
    val code: String = "",
    val type: String = "",
    val status: String = ContainerStatus.INSP.name,
    val lastUpdate: Long = System.currentTimeMillis(),
    val updatedBy: String = "",
    val reeferSerialNumber: String = "",
    val reeferModel: String = "",
    val reeferManufacturer: String = "",
    val reeferYear: String = "",
    val reeferVariant: String = "",                 // "CA" o "STD" — StarCool
    val instruccion: String = WorkInstruction.NONE.displayLabel,
    val observations: String = "",

    @get:PropertyName("archived")
    @set:PropertyName("archived")
    var archived: Boolean = false
) {
    val statusEnum: ContainerStatus get() = ContainerStatus.fromStringOrDefault(status)
    val instruccionEnum: WorkInstruction get() = WorkInstruction.fromStringOrDefault(instruccion)
}

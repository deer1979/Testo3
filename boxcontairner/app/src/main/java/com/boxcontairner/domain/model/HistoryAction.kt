package com.boxcontairner.domain.model

/**
 * Tipo de acción registrada en la trazabilidad de la unidad.
 */
enum class HistoryAction {
    ENTRY,           // Nuevo ingreso (o re-ingreso) de la unidad
    STATUS_CHANGE,   // Cambio de estado de inspección
    EDIT,            // Edición manual de datos
    ARCHIVE;         // Unidad archivada

    companion object {
        fun fromStringOrDefault(value: String?, default: HistoryAction = STATUS_CHANGE): HistoryAction =
            entries.firstOrNull { it.name == value?.trim()?.uppercase() } ?: default
    }
}

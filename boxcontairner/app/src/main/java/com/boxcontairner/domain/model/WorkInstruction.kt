package com.boxcontairner.domain.model

/**
 * Instrucción de trabajo asignada al contenedor.
 */
enum class WorkInstruction(val displayLabel: String) {
    FULL_PTI("Full PTI"),
    VISUAL_CHECK("Visual Check"),
    NONE("SIN INSTRUCCION");

    companion object {
        fun fromStringOrDefault(value: String?, default: WorkInstruction = NONE): WorkInstruction {
            val v = value?.trim() ?: return default
            return entries.firstOrNull { it.displayLabel.equals(v, ignoreCase = true) || it.name == v.uppercase() }
                ?: default
        }
    }
}

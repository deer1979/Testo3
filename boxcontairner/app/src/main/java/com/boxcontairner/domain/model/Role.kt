package com.boxcontairner.domain.model

/**
 * Roles del sistema. Jerarquía: SUPER_ADMIN > ADMIN > OPERATOR.
 */
enum class Role(val displayLabel: String) {
    SUPER_ADMIN("Super Admin"),
    ADMIN("Admin"),
    OPERATOR("Operador");

    /** El usuario actual puede modificar/borrar/archivar contenedores. */
    val canManageContainers: Boolean
        get() = this == SUPER_ADMIN || this == ADMIN

    /** El usuario actual puede gestionar otros usuarios (activar, cambiar rol). */
    val canManageUsers: Boolean
        get() = this == SUPER_ADMIN

    companion object {
        fun fromStringOrDefault(value: String?, default: Role = OPERATOR): Role =
            entries.firstOrNull { it.name == value?.trim()?.uppercase() } ?: default
    }
}

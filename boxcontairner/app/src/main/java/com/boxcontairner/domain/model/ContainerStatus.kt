package com.boxcontairner.domain.model

/**
 * Estado de inspección de un contenedor reefer.
 *
 * Se serializa a String en Firestore/Room usando el .name del enum.
 * Si el valor en remoto no matchea, [fromStringOrDefault] cae al default.
 */
enum class ContainerStatus(val displayLabel: String) {
    INSP("INSP"),
    OK("OK"),
    EST("EST"),
    DMG("DMG");

    companion object {
        fun fromStringOrDefault(value: String?, default: ContainerStatus = INSP): ContainerStatus =
            entries.firstOrNull { it.name == value?.trim()?.uppercase() } ?: default
    }
}

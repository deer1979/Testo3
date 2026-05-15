package com.boxcontairner.util

import com.google.mlkit.vision.text.Text

/**
 * Reconocimiento de placa de identificación de unidad reefer.
 *
 * Soporta los principales fabricantes:
 * - Carrier (modelos 69NT...)
 * - Star Cool (modelos SCI-40...)
 * - Daikin (modelos LXE...)
 * - Thermo King (detección por marca)
 */
data class ReeferData(
    val manufacturer: String = "",
    val model: String = "",
    val serial: String = "",
    val year: String = ""
)

object ReeferPlateScanner {

    fun scan(visionText: Text): ReeferData? {
        val fullText = visionText.text.uppercase().replace("\n", " ")
        if (fullText.isBlank()) return null

        // 1. Modelos por patrón
        val carrierModel = Regex("69NT[0-9]{2}-[A-Z0-9-]{7,}").find(fullText)?.value
        val starCoolModel = Regex("SCI-40-[A-Z0-9]+").find(fullText)?.value
        val daikinModel = Regex("LXE[0-9A-Z]+").find(fullText)?.value
        val model = carrierModel ?: starCoolModel ?: daikinModel ?: ""

        // 2. Seriales
        val carrierSerial = Regex("[A-Z]{3}\\s?[0-9]{8}").find(fullText)?.value
        val starCoolSerial = Regex("QM[0-9]{2}-[0-9]{5}").find(fullText)?.value
        val serial = carrierSerial ?: starCoolSerial ?: ""

        // 3. Año (20XX entre 2010 y 2029)
        val year = Regex("20[12][0-9]").findAll(fullText).lastOrNull()?.value ?: ""

        // 4. Identificación de marca
        val manufacturer = when {
            model.contains("69NT") || fullText.contains("CARRIER") -> "CARRIER"
            model.contains("SCI-40") || fullText.contains("STAR COOL") -> "STAR COOL"
            fullText.contains("DAIKIN") -> "DAIKIN"
            fullText.contains("THERMO KING") -> "THERMO KING"
            else -> ""
        }

        return if (model.isNotBlank() || serial.isNotBlank()) {
            ReeferData(
                manufacturer = manufacturer,
                model = model.trim().removeSuffix("-"),
                serial = serial.replace(" ", "").trim(),
                year = year
            )
        } else null
    }
}

package com.boxcontairner.util

import com.google.mlkit.vision.text.Text

/**
 * Reconocimiento de número de contenedor ISO 6346.
 *
 * Estrategia multi-capa con fallbacks:
 * 1. Texto plano completo (código en 1 línea — caso ideal)
 * 2. Código partido en 2+ líneas (prefijo en una, dígitos en la siguiente)
 * 3. Columna vertical filtrando ruido lateral
 * 4. Concatenación vertical ordenada por posición
 * 5. Limpieza agresiva eliminando todo lo no alfanumérico
 *
 * Cada candidato se sanitiza para corregir confusiones OCR típicas (O↔0, I↔1, etc.)
 * y se valida con el dígito verificador del estándar ISO 6346.
 */
object ISO6346Scanner {

    private val PREFIX_REGEX = Regex("[A-Z]{3}[UJZ]")

    fun scan(visionText: Text): String? {
        findValidId(visionText.text)?.let { return it }
        scanTwoLines(visionText)?.let { return it }
        scanByPrefix(visionText)?.let { return it }

        val elements = visionText.textBlocks.flatMap { it.lines }.flatMap { it.elements }
        val verticalText = elements
            .sortedWith(compareBy({ it.boundingBox?.top ?: 0 }, { it.boundingBox?.left ?: 0 }))
            .joinToString("") { it.text.replace(Regex("[^A-Z0-9]"), "").uppercase() }
        findValidId(verticalText)?.let { return it }

        val megaClean = visionText.text.replace(Regex("[^A-Za-z0-9]"), "").uppercase()
        return findValidId(megaClean)
    }

    private fun scanTwoLines(visionText: Text): String? {
        val lines = visionText.textBlocks.flatMap { it.lines }

        for (i in lines.indices) {
            val lineClean = lines[i].text.replace(Regex("[^A-Z0-9]"), "").uppercase()
            val pm = PREFIX_REGEX.find(lineClean) ?: continue
            val tail = lineClean.substring(pm.range.first)
            if (tail.length >= 11) continue

            val seedBox = lines[i].boundingBox ?: continue
            val seedWidth = (seedBox.right - seedBox.left).coerceAtLeast(1)
            val accumulated = StringBuilder(tail)

            for (j in (i + 1)..minOf(i + 3, lines.lastIndex)) {
                val nextLine = lines[j]
                val nextBox = nextLine.boundingBox
                if (nextBox != null) {
                    val overlapLeft = maxOf(seedBox.left, nextBox.left)
                    val overlapRight = minOf(seedBox.right, nextBox.right)
                    val nextWidth = (nextBox.right - nextBox.left).coerceAtLeast(1)
                    val minWidth = minOf(seedWidth, nextWidth)
                    if (overlapRight - overlapLeft < minWidth * 0.20f) continue
                }

                val nextClean = nextLine.text.replace(Regex("[^A-Z0-9]"), "").uppercase()
                accumulated.append(nextClean)
                findValidId(accumulated.toString())?.let { return it }
                if (accumulated.length >= 15) break
            }
        }
        return null
    }

    private fun scanByPrefix(visionText: Text): String? {
        val elements = visionText.textBlocks.flatMap { it.lines }.flatMap { it.elements }

        for (seedEl in elements) {
            val seedText = seedEl.text.replace(Regex("[^A-Z0-9]"), "").uppercase()
            if (!PREFIX_REGEX.containsMatchIn(seedText)) continue

            val seedBox = seedEl.boundingBox ?: continue
            val seedCenterX = (seedBox.left + seedBox.right) / 2
            val charWidth = (seedBox.right - seedBox.left)
                .coerceAtLeast(1) / seedText.length.coerceAtLeast(1)
            val xTolerance = charWidth * 6

            val columnText = elements
                .filter { el ->
                    val box = el.boundingBox ?: return@filter false
                    val cx = (box.left + box.right) / 2
                    kotlin.math.abs(cx - seedCenterX) < xTolerance
                }
                .sortedWith(compareBy({ it.boundingBox?.top ?: 0 }, { it.boundingBox?.left ?: 0 }))
                .joinToString("") { it.text.replace(Regex("[^A-Z0-9]"), "").uppercase() }

            findValidId(columnText)?.let { return it }
        }
        return null
    }

    /** Versión pública para tests — busca el primer ID válido en un string ya limpio. */
    fun findValidId(text: String): String? {
        val clean = text.replace(Regex("[\\s\\-\\._\\n]"), "").uppercase()
        val pattern = Regex("[A-Z]{3}[UJZ][A-Z0-9]\\d{6}")
        val match = pattern.find(clean)
        return match?.value?.let { candidate ->
            val sanitized = sanitize(candidate)
            if (isValidChecksum(sanitized)) sanitized else null
        }
    }

    /** Corrige confusiones OCR: O↔0, I↔1, S↔5, etc. en sus respectivas posiciones. */
    fun sanitize(raw: String): String {
        val letters = raw.take(4).map { c ->
            when (c) { '0' -> 'O'; '1' -> 'I'; '5' -> 'S'; '8' -> 'B'; '2' -> 'Z'; else -> c }
        }.joinToString("")
        val numbers = raw.drop(4).map { c ->
            when (c) {
                'O', 'D', 'Q' -> '0'
                'I', 'L', 'T' -> '1'
                'S' -> '5'
                'G' -> '6'
                'B' -> '8'
                'Z' -> '2'
                else -> c
            }
        }.joinToString("")
        return letters + numbers
    }

    /** Verificación del dígito de control ISO 6346. */
    fun isValidChecksum(id: String): Boolean {
        if (id.length != 11) return false
        val letterMap = mapOf(
            'A' to 10, 'B' to 12, 'C' to 13, 'D' to 14, 'E' to 15, 'F' to 16,
            'G' to 17, 'H' to 18, 'I' to 19, 'J' to 20, 'K' to 21, 'L' to 23,
            'M' to 24, 'N' to 25, 'O' to 26, 'P' to 27, 'Q' to 28, 'R' to 29,
            'S' to 30, 'T' to 31, 'U' to 32, 'V' to 34, 'W' to 35, 'X' to 36,
            'Y' to 37, 'Z' to 38
        )
        var sum = 0
        for (i in 0 until 10) {
            val c = id[i]
            val value = if (c.isLetter()) letterMap[c] ?: 0 else c.digitToInt()
            sum += value * (1 shl i)
        }
        val mod = sum % 11
        return id.last().digitToInt() == (if (mod == 10) 0 else mod)
    }
}

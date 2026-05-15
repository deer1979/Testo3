package com.boxcontairner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitarios del algoritmo de validación ISO 6346.
 *
 * CSQU3054383 es el ejemplo canónico del estándar ISO 6346 que aparece en la
 * Wikipedia y la documentación oficial. Si este test pasa, el algoritmo de
 * check digit está implementado correctamente.
 */
class ISO6346ScannerTest {

    @Test
    fun `checksum valido para codigo canonico CSQU3054383`() {
        assertTrue(ISO6346Scanner.isValidChecksum("CSQU3054383"))
    }

    @Test
    fun `checksum invalido si se cambia el ultimo digito`() {
        assertFalse(ISO6346Scanner.isValidChecksum("CSQU3054384"))
        assertFalse(ISO6346Scanner.isValidChecksum("CSQU3054380"))
    }

    @Test
    fun `checksum rechaza longitud incorrecta`() {
        assertFalse(ISO6346Scanner.isValidChecksum(""))
        assertFalse(ISO6346Scanner.isValidChecksum("CSQU305438"))      // 10 chars
        assertFalse(ISO6346Scanner.isValidChecksum("CSQU30543830"))    // 12 chars
    }

    @Test
    fun `findValidId encuentra el codigo en texto plano`() {
        assertEquals("CSQU3054383", ISO6346Scanner.findValidId("CSQU3054383"))
    }

    @Test
    fun `findValidId tolera espacios y guiones`() {
        assertEquals("CSQU3054383", ISO6346Scanner.findValidId("CSQU 305 438-3"))
        assertEquals("CSQU3054383", ISO6346Scanner.findValidId("CSQU-305438-3"))
        assertEquals("CSQU3054383", ISO6346Scanner.findValidId("CSQU\n305438\n3"))
    }

    @Test
    fun `findValidId devuelve null si no hay codigo valido`() {
        assertNull(ISO6346Scanner.findValidId("NO HAY NINGUN CONTENEDOR ACA"))
        assertNull(ISO6346Scanner.findValidId("CSQU3054384"))  // checksum roto
    }

    @Test
    fun `sanitize corrige cero por O en posicion de letra`() {
        // '0' en las primeras 4 posiciones se convierte a 'O'
        assertEquals("OSQU3054383", ISO6346Scanner.sanitize("0SQU3054383"))
    }

    @Test
    fun `sanitize corrige O por cero en posicion de digito`() {
        // 'O' en las posiciones de dígitos se convierte a '0'
        assertEquals("CSQU3054383", ISO6346Scanner.sanitize("CSQU3O54383"))
    }
}

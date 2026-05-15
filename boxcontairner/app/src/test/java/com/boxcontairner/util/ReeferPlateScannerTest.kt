package com.boxcontairner.util

/**
 * Los tests de ReeferPlateScanner requieren construir un objeto Text de ML Kit, que es
 * difícil de mockear sin la dependencia completa del SDK. Para no acoplar tests
 * unitarios a Android, esto se cubrirá con tests instrumentados (androidTest) en una
 * siguiente iteración. Por ahora dejamos esta clase como placeholder documentado.
 *
 * Si se quiere testear puro, conviene extraer los regex a una función `parsePlateText(s: String)`
 * que reciba el texto plano, y testear esa función con strings.
 */
class ReeferPlateScannerTest

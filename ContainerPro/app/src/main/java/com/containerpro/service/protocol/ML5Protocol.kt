package com.containerpro.service.protocol

/**
 * ML5 TCP Protocol — Carrier Transicold
 *
 * Protocolo propietario deducido del tráfico TCP del APK original.
 * Puerto estándar: 10001 (configurable en firmware del controlador).
 *
 * Frame structure:
 * ┌──────────┬──────────┬──────────┬────────────┬──────────┐
 * │ STX (1B) │ CMD (1B) │ ADDR(1B) │ DATA (nB)  │ CRC (2B) │
 * └──────────┴──────────┴──────────┴────────────┴──────────┘
 *
 * STX  = 0x02 (Start of frame)
 * CMD  = Command byte
 * ADDR = Register address
 * DATA = Variable length payload
 * CRC  = CRC-16/MODBUS over CMD+ADDR+DATA
 */

object ML5Protocol {

    const val DEFAULT_PORT     = 10001
    const val TIMEOUT_MS       = 5000L
    const val RECONNECT_DELAY  = 3000L
    const val POLL_INTERVAL_MS = 1000L

    const val STX = 0x02.toByte()
    const val ETX = 0x03.toByte()

    // ── Command bytes ──────────────────────────────────────
    object CMD {
        const val READ_REGISTER  = 0x03.toByte()   // Read holding registers
        const val WRITE_REGISTER = 0x06.toByte()   // Write single register
        const val WRITE_COIL     = 0x05.toByte()   // Write single coil (relay ON/OFF)
        const val READ_COILS     = 0x01.toByte()   // Read coil status
        const val READ_INPUTS    = 0x02.toByte()   // Read discrete inputs
        const val FORCE_COILS    = 0x0F.toByte()   // Force multiple coils
        const val DIAGNOSTICS    = 0x08.toByte()   // Diagnostics / ping
    }

    // ── Coil addresses (relays) ────────────────────────────
    /**
     * Coil = salida digital del controlador.
     * Cada coil activa un relé que controla un componente.
     * Valor ON = 0xFF00, OFF = 0x0000 (estándar Modbus)
     */
    object Coil {
        const val COMPRESSOR          = 0x0001  // Relé compresor
        const val EVAPORATOR_FAN_LOW  = 0x0002  // Fan evaporador velocidad baja
        const val EVAPORATOR_FAN_HIGH = 0x0003  // Fan evaporador velocidad alta
        const val CONDENSER_FAN       = 0x0004  // Fan condensador
        const val DEFROST_HEATER_1    = 0x0005  // Calefactor deshielo 1
        const val DEFROST_HEATER_2    = 0x0006  // Calefactor deshielo 2
        const val UNLOADER_1          = 0x0007  // Descargador de compresor 1
        const val UNLOADER_2          = 0x0008  // Descargador de compresor 2
        const val LIQUID_LINE_SOLENOID= 0x0009  // Solenoide línea de líquido
        const val HOT_GAS_BYPASS      = 0x000A  // Bypass gas caliente
        const val EVAP_DRAIN          = 0x000B  // Drenaje evaporador
        const val ALARM_LIGHT         = 0x000C  // Luz de alarma externa
    }

    // ── Register addresses (read) ──────────────────────────
    /**
     * Holding registers — valores de 16 bits.
     * Escala: valores enteros, dividir por factor según sensor.
     */
    object Register {
        // Temperatures (factor /10 → °C)
        const val SUPPLY_AIR_TEMP     = 0x0100  // Temperatura supply air
        const val RETURN_AIR_TEMP     = 0x0101  // Temperatura return air
        const val SETPOINT            = 0x0102  // Setpoint actual
        const val EVAP_COIL_TEMP      = 0x0103  // Temperatura bobina evaporador
        const val CONDENSER_COIL_TEMP = 0x0104  // Temperatura bobina condensador
        const val AMBIENT_TEMP        = 0x0105  // Temperatura ambiente exterior
        const val DISCHARGE_TEMP      = 0x0106  // Temperatura descarga compresor
        const val SUCTION_TEMP        = 0x0107  // Temperatura succión compresor

        // Electrical (voltaje factor /10 → V, amperaje factor /100 → A)
        const val VOLTAGE_L1          = 0x0200  // Voltaje línea 1 (V)
        const val VOLTAGE_L2          = 0x0201  // Voltaje línea 2 (V)
        const val VOLTAGE_L3          = 0x0202  // Voltaje línea 3 (V)
        const val CURRENT_COMPRESSOR  = 0x0203  // Amperaje compresor (A)
        const val CURRENT_EVAP_FAN    = 0x0204  // Amperaje fan evaporador (A)
        const val CURRENT_COND_FAN    = 0x0205  // Amperaje fan condensador (A)
        const val POWER_TOTAL         = 0x0206  // Potencia total (kW, factor /100)
        const val FREQUENCY           = 0x0207  // Frecuencia red (Hz, factor /10)
        const val POWER_FACTOR        = 0x0208  // Factor de potencia (factor /100)

        // Pressures (factor /10 → PSI o Bar)
        const val SUCTION_PRESSURE    = 0x0300  // Presión succión
        const val DISCHARGE_PRESSURE  = 0x0301  // Presión descarga
        const val OIL_PRESSURE        = 0x0302  // Presión aceite

        // Status
        const val CONTROLLER_STATUS   = 0x0400  // Estado general controlador
        const val ACTIVE_ALARMS_COUNT = 0x0401  // Número de alarmas activas
        const val OPERATING_HOURS     = 0x0402  // Horas de operación compresor
        const val DEFROST_STATUS      = 0x0403  // Estado ciclo deshielo
        const val HUMIDITY            = 0x0404  // Humedad (factor /10 → %)
        const val CO2_LEVEL           = 0x0405  // CO2 ppm (NaturaLine)

        // Alarm codes (0x0500..0x051F = hasta 32 alarmas activas)
        const val ALARM_BASE          = 0x0500
    }

    // ── Coil value constants ───────────────────────────────
    const val COIL_ON  : Short = 0xFF00.toShort()
    const val COIL_OFF : Short = 0x0000.toShort()

    // ── Service mode guard ─────────────────────────────────
    /**
     * El controlador ML5 requiere activar "Modo Servicio"
     * antes de permitir comandos manuales de relés.
     * Esto previene activaciones accidentales.
     */
    const val SERVICE_MODE_REGISTER = 0x0010
    const val SERVICE_MODE_ENABLE   = 0xA5A5.toShort()   // Magic word
    const val SERVICE_MODE_DISABLE  = 0x0000.toShort()

    // ── Frame builder ──────────────────────────────────────
    fun buildWriteCoilFrame(coilAddress: Int, enable: Boolean): ByteArray {
        val value = if (enable) COIL_ON else COIL_OFF
        val payload = byteArrayOf(
            STX,
            CMD.WRITE_COIL,
            (coilAddress shr 8).toByte(),
            (coilAddress and 0xFF).toByte(),
            (value.toInt() shr 8).toByte(),
            (value.toInt() and 0xFF).toByte()
        )
        val crc = crc16(payload.drop(1).toByteArray())
        return payload + byteArrayOf((crc shr 8).toByte(), (crc and 0xFF).toByte(), ETX)
    }

    fun buildReadRegistersFrame(startAddr: Int, count: Int = 1): ByteArray {
        val payload = byteArrayOf(
            STX,
            CMD.READ_REGISTER,
            (startAddr shr 8).toByte(),
            (startAddr and 0xFF).toByte(),
            (count shr 8).toByte(),
            (count and 0xFF).toByte()
        )
        val crc = crc16(payload.drop(1).toByteArray())
        return payload + byteArrayOf((crc shr 8).toByte(), (crc and 0xFF).toByte(), ETX)
    }

    fun buildServiceModeFrame(enable: Boolean): ByteArray {
        val value = if (enable) SERVICE_MODE_ENABLE else SERVICE_MODE_DISABLE
        val payload = byteArrayOf(
            STX,
            CMD.WRITE_REGISTER,
            (SERVICE_MODE_REGISTER shr 8).toByte(),
            (SERVICE_MODE_REGISTER and 0xFF).toByte(),
            (value.toInt() shr 8).toByte(),
            (value.toInt() and 0xFF).toByte()
        )
        val crc = crc16(payload.drop(1).toByteArray())
        return payload + byteArrayOf((crc shr 8).toByte(), (crc and 0xFF).toByte(), ETX)
    }

    fun buildReadCoilsFrame(startAddr: Int, count: Int = 16): ByteArray {
        val payload = byteArrayOf(
            STX,
            CMD.READ_COILS,
            (startAddr shr 8).toByte(),
            (startAddr and 0xFF).toByte(),
            (count shr 8).toByte(),
            (count and 0xFF).toByte()
        )
        val crc = crc16(payload.drop(1).toByteArray())
        return payload + byteArrayOf((crc shr 8).toByte(), (crc and 0xFF).toByte(), ETX)
    }

    // ── Response parser ────────────────────────────────────
    fun parseRegisterResponse(data: ByteArray): List<Int> {
        if (data.size < 5) return emptyList()
        // Skip STX + CMD + byte count
        val byteCount = data[2].toInt() and 0xFF
        val values    = mutableListOf<Int>()
        var i         = 3
        while (i + 1 < 3 + byteCount) {
            val hi = data[i].toInt() and 0xFF
            val lo = data[i + 1].toInt() and 0xFF
            values.add((hi shl 8) or lo)
            i += 2
        }
        return values
    }

    fun parseCoilResponse(data: ByteArray): List<Boolean> {
        if (data.size < 4) return emptyList()
        val byteCount = data[2].toInt() and 0xFF
        val result    = mutableListOf<Boolean>()
        for (b in 3 until 3 + byteCount) {
            val byte = data[b].toInt() and 0xFF
            for (bit in 0..7) result.add((byte shr bit) and 1 == 1)
        }
        return result
    }

    // ── CRC-16/MODBUS ──────────────────────────────────────
    fun crc16(data: ByteArray): Int {
        var crc = 0xFFFF
        for (byte in data) {
            crc = crc xor (byte.toInt() and 0xFF)
            repeat(8) {
                crc = if (crc and 1 != 0) (crc shr 1) xor 0xA001 else crc shr 1
            }
        }
        return crc
    }
}

// ── Relay descriptor for UI ────────────────────────────────
data class RelayDefinition(
    val id          : String,
    val coilAddress : Int,
    val displayName : String,
    val description : String,
    val group       : RelayGroup,
    val isDangerous : Boolean = false,   // requires double confirmation
    val iconName    : String  = "electric_bolt"
)

enum class RelayGroup(val label: String) {
    COMPRESSION ("Compresor"),
    VENTILATION ("Ventilación"),
    DEFROST     ("Deshielo"),
    REFRIGERANT ("Refrigerante"),
    AUXILIARY   ("Auxiliar")
}

/** All controllable relays for ML5 */
val ML5_RELAYS = listOf(
    RelayDefinition(
        id          = "compressor",
        coilAddress = ML5Protocol.Coil.COMPRESSOR,
        displayName = "Compresor",
        description = "Bobina principal del compresor",
        group       = RelayGroup.COMPRESSION,
        isDangerous = true
    ),
    RelayDefinition(
        id          = "unloader_1",
        coilAddress = ML5Protocol.Coil.UNLOADER_1,
        displayName = "Descargador 1",
        description = "Válvula descargadora cilindro 1",
        group       = RelayGroup.COMPRESSION,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "unloader_2",
        coilAddress = ML5Protocol.Coil.UNLOADER_2,
        displayName = "Descargador 2",
        description = "Válvula descargadora cilindro 2",
        group       = RelayGroup.COMPRESSION,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "evap_fan_low",
        coilAddress = ML5Protocol.Coil.EVAPORATOR_FAN_LOW,
        displayName = "Fan Evap. Bajo",
        description = "Velocidad baja del ventilador evaporador",
        group       = RelayGroup.VENTILATION,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "evap_fan_high",
        coilAddress = ML5Protocol.Coil.EVAPORATOR_FAN_HIGH,
        displayName = "Fan Evap. Alto",
        description = "Velocidad alta del ventilador evaporador",
        group       = RelayGroup.VENTILATION,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "cond_fan",
        coilAddress = ML5Protocol.Coil.CONDENSER_FAN,
        displayName = "Fan Condensador",
        description = "Ventilador del condensador",
        group       = RelayGroup.VENTILATION,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "defrost_1",
        coilAddress = ML5Protocol.Coil.DEFROST_HEATER_1,
        displayName = "Calefactor Deshielo 1",
        description = "Resistencia de deshielo 1",
        group       = RelayGroup.DEFROST,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "defrost_2",
        coilAddress = ML5Protocol.Coil.DEFROST_HEATER_2,
        displayName = "Calefactor Deshielo 2",
        description = "Resistencia de deshielo 2",
        group       = RelayGroup.DEFROST,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "liquid_solenoid",
        coilAddress = ML5Protocol.Coil.LIQUID_LINE_SOLENOID,
        displayName = "Solenoide Líquido",
        description = "Solenoide línea de líquido refrigerante",
        group       = RelayGroup.REFRIGERANT,
        isDangerous = true
    ),
    RelayDefinition(
        id          = "hot_gas",
        coilAddress = ML5Protocol.Coil.HOT_GAS_BYPASS,
        displayName = "Bypass Gas Caliente",
        description = "Válvula de bypass gas caliente",
        group       = RelayGroup.REFRIGERANT,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "evap_drain",
        coilAddress = ML5Protocol.Coil.EVAP_DRAIN,
        displayName = "Drenaje Evaporador",
        description = "Válvula de drenaje del evaporador",
        group       = RelayGroup.AUXILIARY,
        isDangerous = false
    ),
    RelayDefinition(
        id          = "alarm_light",
        coilAddress = ML5Protocol.Coil.ALARM_LIGHT,
        displayName = "Luz Alarma",
        description = "Luz de alarma externa del contenedor",
        group       = RelayGroup.AUXILIARY,
        isDangerous = false
    ),
)

package com.containerpro.service.protocol

/** ML5 TCP protocol constants (port 10001). Frame: STX CMD ADDR DATA CRC16 ETX */
object ML5Protocol {
    const val PORT      = 10001
    const val TIMEOUT_MS = 5_000L

    const val STX: Byte = 0x02
    const val ETX: Byte = 0x03

    // Commands
    const val CMD_READ_COIL    = 0x01.toByte()
    const val CMD_READ_REG     = 0x03.toByte()
    const val CMD_WRITE_COIL   = 0x05.toByte()
    const val CMD_WRITE_REG    = 0x06.toByte()

    // Coil addresses (relays)
    const val COIL_COMPRESSOR  = 0x0001
    const val COIL_UNLOADER1   = 0x0002
    const val COIL_UNLOADER2   = 0x0003
    const val COIL_FAN_EVAP1   = 0x0004
    const val COIL_FAN_EVAP2   = 0x0005
    const val COIL_FAN_COND    = 0x0006
    const val COIL_HTR_EVAP    = 0x0007
    const val COIL_HTR_DRAIN   = 0x0008
    const val COIL_SOL_LIQ     = 0x0009
    const val COIL_SOL_SUCT    = 0x000A
    const val COIL_AUX_HTR     = 0x000B
    const val COIL_ALARM        = 0x000C

    // Register addresses — temperatures (×10 = °C)
    const val REG_SUPPLY_AIR   = 0x0100
    const val REG_RETURN_AIR   = 0x0101
    const val REG_SETPOINT     = 0x0102
    const val REG_EVAP_COIL    = 0x0103
    const val REG_COND_COIL    = 0x0104
    const val REG_AMBIENT      = 0x0105
    const val REG_DISCHARGE    = 0x0106
    const val REG_SUCTION      = 0x0107

    // Register addresses — electrical
    const val REG_VOLTAGE_L1   = 0x0200
    const val REG_VOLTAGE_L2   = 0x0201
    const val REG_VOLTAGE_L3   = 0x0202
    const val REG_CURRENT_COMP = 0x0203
    const val REG_POWER_KW     = 0x0204
    const val REG_FREQUENCY    = 0x0205
    const val REG_POWER_FACTOR = 0x0206

    // Service mode register
    const val REG_SERVICE_MODE  = 0x0010
    const val SERVICE_MAGIC     = 0xA5A5

    /** CRC-16/Modbus. */
    fun crc16(data: ByteArray): Int {
        var crc = 0xFFFF
        for (b in data) {
            crc = crc xor (b.toInt() and 0xFF)
            repeat(8) {
                crc = if (crc and 0x0001 != 0) (crc shr 1) xor 0xA001 else crc shr 1
            }
        }
        return crc and 0xFFFF
    }

    fun buildWriteCoilFrame(coilAddress: Int, activate: Boolean): ByteArray {
        val payload = byteArrayOf(
            STX,
            CMD_WRITE_COIL,
            ((coilAddress shr 8) and 0xFF).toByte(),
            (coilAddress and 0xFF).toByte(),
            if (activate) 0xFF.toByte() else 0x00,
            0x00,
        )
        val crc = crc16(payload)
        return payload + byteArrayOf(
            (crc and 0xFF).toByte(),
            ((crc shr 8) and 0xFF).toByte(),
            ETX,
        )
    }

    fun buildReadRegsFrame(startAddress: Int, count: Int): ByteArray {
        val payload = byteArrayOf(
            STX,
            CMD_READ_REG,
            ((startAddress shr 8) and 0xFF).toByte(),
            (startAddress and 0xFF).toByte(),
            ((count shr 8) and 0xFF).toByte(),
            (count and 0xFF).toByte(),
        )
        val crc = crc16(payload)
        return payload + byteArrayOf(
            (crc and 0xFF).toByte(),
            ((crc shr 8) and 0xFF).toByte(),
            ETX,
        )
    }
}

package com.containerpro.service.protocol

import com.containerpro.model.ElectricalData
import com.containerpro.model.TemperatureData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class ML5TcpService(private val ipAddress: String) {

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private suspend fun ensureConnected() = withContext(Dispatchers.IO) {
        if (socket?.isConnected != true) {
            socket = Socket(ipAddress, ML5Protocol.PORT).also {
                it.soTimeout = ML5Protocol.TIMEOUT_MS.toInt()
                outputStream = it.getOutputStream()
                inputStream  = it.getInputStream()
            }
        }
    }

    suspend fun setServiceMode(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            ensureConnected()
            val value = if (enable) ML5Protocol.SERVICE_MAGIC else 0x0000
            val frame = ML5Protocol.buildWriteCoilFrame(ML5Protocol.REG_SERVICE_MODE, enable)
            outputStream?.write(frame)
            true
        }.getOrDefault(false)
    }

    suspend fun setRelay(address: Int, activate: Boolean): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            ensureConnected()
            val frame = ML5Protocol.buildWriteCoilFrame(address, activate)
            outputStream?.write(frame)
            true
        }.getOrDefault(false)
    }

    suspend fun emergencyStop(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            ensureConnected()
            // Write OFF to all relay coils
            for (addr in 0x0001..0x000C) {
                val frame = ML5Protocol.buildWriteCoilFrame(addr, false)
                outputStream?.write(frame)
                Thread.sleep(20)
            }
            true
        }.getOrDefault(false)
    }

    suspend fun readTemperatureData(): TemperatureData = withContext(Dispatchers.IO) {
        runCatching {
            ensureConnected()
            val frame = ML5Protocol.buildReadRegsFrame(ML5Protocol.REG_SUPPLY_AIR, 8)
            outputStream?.write(frame)
            val buf = ByteArray(32)
            val n = inputStream?.read(buf) ?: 0
            if (n < 20) return@runCatching TemperatureData()
            fun reg(i: Int): Float {
                val raw = ((buf[3 + i * 2].toInt() and 0xFF) shl 8) or (buf[4 + i * 2].toInt() and 0xFF)
                return raw.toShort() / 10f
            }
            TemperatureData(
                supplyAir = reg(0), returnAir = reg(1), setpoint  = reg(2),
                evapCoil  = reg(3), condCoil  = reg(4), ambient   = reg(5),
                discharge = reg(6), suction   = reg(7),
            )
        }.getOrDefault(TemperatureData())
    }

    suspend fun readElectricalData(): ElectricalData = withContext(Dispatchers.IO) {
        runCatching {
            ensureConnected()
            val frame = ML5Protocol.buildReadRegsFrame(ML5Protocol.REG_VOLTAGE_L1, 7)
            outputStream?.write(frame)
            val buf = ByteArray(32)
            val n = inputStream?.read(buf) ?: 0
            if (n < 20) return@runCatching ElectricalData()
            fun reg(i: Int): Float {
                val raw = ((buf[3 + i * 2].toInt() and 0xFF) shl 8) or (buf[4 + i * 2].toInt() and 0xFF)
                return raw.toFloat() / 10f
            }
            ElectricalData(
                voltageL1   = reg(0), voltageL2   = reg(1), voltageL3   = reg(2),
                currentComp = reg(3), powerKw     = reg(4), frequencyHz = reg(5),
                powerFactor = reg(6),
            )
        }.getOrDefault(ElectricalData())
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching {
            outputStream?.close()
            inputStream?.close()
            socket?.close()
            socket = null
        }
    }
}

package com.containerpro.service.protocol

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Servicio TCP para comunicación con controlador ML5.
 *
 * Flujo de trabajo:
 * 1. connect(ip) → abre socket TCP al puerto 10001
 * 2. enableServiceMode() → habilita modo servicio en el controlador
 * 3. startPolling() → lee registros eléctricos y de estado cada 1s
 * 4. writeCoil(addr, on) → activa/desactiva relé
 * 5. disableServiceMode() → vuelve al modo automático
 * 6. disconnect() → cierra socket
 */
class ML5TcpService {

    private var socket       : Socket?       = null
    private var outputStream : OutputStream? = null
    private var inputStream  : InputStream?  = null
    private var pollingJob   : Job?          = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── State flows ────────────────────────────────────────
    private val _connectionState = MutableStateFlow(TcpConnectionState.DISCONNECTED)
    val connectionState: StateFlow<TcpConnectionState> = _connectionState.asStateFlow()

    private val _electricalData = MutableStateFlow<ElectricalData?>(null)
    val electricalData: StateFlow<ElectricalData?> = _electricalData.asStateFlow()

    private val _coilStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val coilStates: StateFlow<Map<Int, Boolean>> = _coilStates.asStateFlow()

    private val _serviceModeActive = MutableStateFlow(false)
    val serviceModeActive: StateFlow<Boolean> = _serviceModeActive.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // ── Connect ────────────────────────────────────────────
    suspend fun connect(ipAddress: String, port: Int = ML5Protocol.DEFAULT_PORT): Boolean =
        withContext(Dispatchers.IO) {
            try {
                _connectionState.value = TcpConnectionState.CONNECTING
                socket = Socket().apply {
                    soTimeout = ML5Protocol.TIMEOUT_MS.toInt()
                    connect(InetSocketAddress(ipAddress, port), ML5Protocol.TIMEOUT_MS.toInt())
                }
                outputStream = socket!!.getOutputStream()
                inputStream  = socket!!.getInputStream()
                _connectionState.value = TcpConnectionState.CONNECTED
                _lastError.value = null
                true
            } catch (e: Exception) {
                _connectionState.value = TcpConnectionState.ERROR
                _lastError.value       = "Error de conexión: ${e.message}"
                false
            }
        }

    // ── Disconnect ─────────────────────────────────────────
    fun disconnect() {
        pollingJob?.cancel()
        disableServiceModeSync()
        try {
            socket?.close()
        } catch (_: Exception) {}
        socket       = null
        outputStream = null
        inputStream  = null
        _connectionState.value  = TcpConnectionState.DISCONNECTED
        _serviceModeActive.value = false
        _electricalData.value    = null
        _coilStates.value        = emptyMap()
    }

    // ── Service mode ───────────────────────────────────────
    suspend fun enableServiceMode(): Boolean = withContext(Dispatchers.IO) {
        val frame  = ML5Protocol.buildServiceModeFrame(true)
        val result = sendAndReceive(frame)
        if (result != null) {
            _serviceModeActive.value = true
            true
        } else false
    }

    suspend fun disableServiceMode(): Boolean = withContext(Dispatchers.IO) {
        disableServiceModeSync()
    }

    private fun disableServiceModeSync(): Boolean {
        val frame  = ML5Protocol.buildServiceModeFrame(false)
        val result = sendAndReceiveBlocking(frame)
        _serviceModeActive.value = false
        return result != null
    }

    // ── Relay control ──────────────────────────────────────
    suspend fun writeCoil(coilAddress: Int, enable: Boolean): CoilResult =
        withContext(Dispatchers.IO) {
            if (!_serviceModeActive.value) {
                return@withContext CoilResult.ERROR_NO_SERVICE_MODE
            }
            val frame  = ML5Protocol.buildWriteCoilFrame(coilAddress, enable)
            val result = sendAndReceive(frame)
            if (result != null) {
                // Update local coil state
                val current = _coilStates.value.toMutableMap()
                current[coilAddress] = enable
                _coilStates.value = current
                CoilResult.SUCCESS
            } else {
                CoilResult.ERROR_TIMEOUT
            }
        }

    // ── Read all coil states ───────────────────────────────
    suspend fun refreshCoilStates(): Boolean = withContext(Dispatchers.IO) {
        val frame  = ML5Protocol.buildReadCoilsFrame(0x0001, 12)
        val result = sendAndReceive(frame) ?: return@withContext false
        val states = ML5Protocol.parseCoilResponse(result)
        val map    = mutableMapOf<Int, Boolean>()
        states.forEachIndexed { index, state ->
            map[index + 1] = state   // coil addresses start at 1
        }
        _coilStates.value = map
        true
    }

    // ── Polling ────────────────────────────────────────────
    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive && _connectionState.value == TcpConnectionState.CONNECTED) {
                try {
                    readElectricalData()
                    refreshCoilStates()
                } catch (e: Exception) {
                    _lastError.value = "Error de lectura: ${e.message}"
                }
                delay(ML5Protocol.POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // ── Read electrical registers ──────────────────────────
    private suspend fun readElectricalData() {
        // Read 9 electrical registers starting at VOLTAGE_L1 (0x0200)
        val elecFrame = ML5Protocol.buildReadRegistersFrame(
            ML5Protocol.Register.VOLTAGE_L1, count = 9)
        val elecRaw   = sendAndReceive(elecFrame)

        // Read temperature registers (0x0100, count = 8)
        val tempFrame = ML5Protocol.buildReadRegistersFrame(
            ML5Protocol.Register.SUPPLY_AIR_TEMP, count = 8)
        val tempRaw   = sendAndReceive(tempFrame)

        // Read status registers (0x0400, count = 6)
        val statFrame = ML5Protocol.buildReadRegistersFrame(
            ML5Protocol.Register.CONTROLLER_STATUS, count = 6)
        val statRaw   = sendAndReceive(statFrame)

        val elec = elecRaw?.let { ML5Protocol.parseRegisterResponse(it) }
        val temp = tempRaw?.let { ML5Protocol.parseRegisterResponse(it) }
        val stat = statRaw?.let { ML5Protocol.parseRegisterResponse(it) }

        if (elec != null && temp != null) {
            _electricalData.value = ElectricalData(
                voltageL1           = elec.getOrNull(0)?.div(10f)  ?: 0f,
                voltageL2           = elec.getOrNull(1)?.div(10f)  ?: 0f,
                voltageL3           = elec.getOrNull(2)?.div(10f)  ?: 0f,
                currentCompressor   = elec.getOrNull(3)?.div(100f) ?: 0f,
                currentEvapFan      = elec.getOrNull(4)?.div(100f) ?: 0f,
                currentCondFan      = elec.getOrNull(5)?.div(100f) ?: 0f,
                powerTotal          = elec.getOrNull(6)?.div(100f) ?: 0f,
                frequency           = elec.getOrNull(7)?.div(10f)  ?: 0f,
                powerFactor         = elec.getOrNull(8)?.div(100f) ?: 0f,
                supplyAirTemp       = toSignedTemp(temp.getOrNull(0) ?: 0),
                returnAirTemp       = toSignedTemp(temp.getOrNull(1) ?: 0),
                setPoint            = toSignedTemp(temp.getOrNull(2) ?: 0),
                evapCoilTemp        = toSignedTemp(temp.getOrNull(3) ?: 0),
                condenserCoilTemp   = toSignedTemp(temp.getOrNull(4) ?: 0),
                ambientTemp         = toSignedTemp(temp.getOrNull(5) ?: 0),
                dischargeTemp       = toSignedTemp(temp.getOrNull(6) ?: 0),
                suctionTemp         = toSignedTemp(temp.getOrNull(7) ?: 0),
                activeAlarms        = stat?.getOrNull(1) ?: 0,
                operatingHours      = stat?.getOrNull(2) ?: 0
            )
        }
    }

    // Signed 16-bit integer → Float °C (values are x10)
    private fun toSignedTemp(raw: Int): Float {
        val signed = if (raw > 32767) raw - 65536 else raw
        return signed / 10f
    }

    // ── Low-level send/receive ─────────────────────────────
    private suspend fun sendAndReceive(frame: ByteArray): ByteArray? =
        withContext(Dispatchers.IO) {
            sendAndReceiveBlocking(frame)
        }

    private fun sendAndReceiveBlocking(frame: ByteArray): ByteArray? {
        return try {
            val out = outputStream ?: return null
            val inp = inputStream  ?: return null
            out.write(frame)
            out.flush()
            val buffer   = ByteArray(256)
            val bytesRead = inp.read(buffer)
            if (bytesRead > 0) buffer.take(bytesRead).toByteArray() else null
        } catch (e: Exception) {
            _lastError.value = e.message
            null
        }
    }

    fun release() {
        scope.cancel()
        disconnect()
    }
}

// ── Data classes ───────────────────────────────────────────
data class ElectricalData(
    // Voltages
    val voltageL1         : Float,   // V
    val voltageL2         : Float,   // V
    val voltageL3         : Float,   // V
    // Currents
    val currentCompressor : Float,   // A
    val currentEvapFan    : Float,   // A
    val currentCondFan    : Float,   // A
    // Power
    val powerTotal        : Float,   // kW
    val frequency         : Float,   // Hz
    val powerFactor       : Float,   // 0.0..1.0
    // Temperatures
    val supplyAirTemp     : Float,   // °C
    val returnAirTemp     : Float,   // °C
    val setPoint          : Float,   // °C
    val evapCoilTemp      : Float,   // °C
    val condenserCoilTemp : Float,   // °C
    val ambientTemp       : Float,   // °C
    val dischargeTemp     : Float,   // °C
    val suctionTemp       : Float,   // °C
    // Status
    val activeAlarms      : Int,
    val operatingHours    : Int
) {
    val voltageAvg: Float get() = (voltageL1 + voltageL2 + voltageL3) / 3f
    val voltageImbalance: Float get() {
        val avg = voltageAvg
        if (avg == 0f) return 0f
        val maxDev = maxOf(
            kotlin.math.abs(voltageL1 - avg),
            kotlin.math.abs(voltageL2 - avg),
            kotlin.math.abs(voltageL3 - avg)
        )
        return (maxDev / avg) * 100f
    }
}

enum class TcpConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

enum class CoilResult {
    SUCCESS,
    ERROR_NO_SERVICE_MODE,
    ERROR_TIMEOUT,
    ERROR_REJECTED           // Controller refused the command
}

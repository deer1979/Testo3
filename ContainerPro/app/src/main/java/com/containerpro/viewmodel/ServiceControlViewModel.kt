package com.containerpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.containerpro.service.protocol.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServiceControlViewModel(application: Application) : AndroidViewModel(application) {

    val tcpService = ML5TcpService()

    // ── Forwarded flows ────────────────────────────────────
    val connectionState  : StateFlow<TcpConnectionState> = tcpService.connectionState
    val electricalData   : StateFlow<ElectricalData?>    = tcpService.electricalData
    val coilStates       : StateFlow<Map<Int, Boolean>>  = tcpService.coilStates
    val serviceModeActive: StateFlow<Boolean>            = tcpService.serviceModeActive
    val lastError        : StateFlow<String?>            = tcpService.lastError

    // ── UI state ───────────────────────────────────────────
    private val _pendingRelay     = MutableStateFlow<RelayDefinition?>(null)
    val pendingRelay: StateFlow<RelayDefinition?> = _pendingRelay.asStateFlow()

    private val _pendingAction    = MutableStateFlow<Boolean>(false)
    val pendingAction: StateFlow<Boolean> = _pendingAction.asStateFlow()

    private val _actionResult     = MutableStateFlow<ActionFeedback?>(null)
    val actionResult: StateFlow<ActionFeedback?> = _actionResult.asStateFlow()

    private val _isConnecting     = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    // ── TCP connect ────────────────────────────────────────
    fun connectToUnit(ipAddress: String) {
        viewModelScope.launch {
            _isConnecting.value = true
            val ok = tcpService.connect(ipAddress)
            if (ok) {
                tcpService.refreshCoilStates()
                tcpService.startPolling()
            }
            _isConnecting.value = false
        }
    }

    fun disconnectFromUnit() {
        tcpService.stopPolling()
        viewModelScope.launch { tcpService.disableServiceMode() }
        tcpService.disconnect()
    }

    // ── Service mode ───────────────────────────────────────
    fun activateServiceMode() {
        viewModelScope.launch {
            val ok = tcpService.enableServiceMode()
            if (!ok) {
                _actionResult.value = ActionFeedback(
                    success = false,
                    message = "No se pudo activar Modo Servicio"
                )
            }
        }
    }

    fun deactivateServiceMode() {
        viewModelScope.launch {
            tcpService.disableServiceMode()
        }
    }

    // ── Relay control ──────────────────────────────────────
    /**
     * Step 1: User taps relay button → show confirmation dialog
     */
    fun requestRelayToggle(relay: RelayDefinition, enable: Boolean) {
        _pendingRelay.value  = relay
        _pendingAction.value = enable
    }

    fun cancelPendingRelay() {
        _pendingRelay.value = null
    }

    /**
     * Step 2: User confirms → execute command
     */
    fun confirmRelayToggle() {
        val relay  = _pendingRelay.value  ?: return
        val enable = _pendingAction.value
        _pendingRelay.value = null

        viewModelScope.launch {
            val result = tcpService.writeCoil(relay.coilAddress, enable)
            _actionResult.value = when (result) {
                CoilResult.SUCCESS -> ActionFeedback(
                    success = true,
                    message = "${relay.displayName} — ${if (enable) "ACTIVADO ✓" else "DESACTIVADO ✓"}"
                )
                CoilResult.ERROR_NO_SERVICE_MODE -> ActionFeedback(
                    success = false,
                    message = "Active el Modo Servicio primero"
                )
                CoilResult.ERROR_TIMEOUT -> ActionFeedback(
                    success = false,
                    message = "Sin respuesta del controlador"
                )
                CoilResult.ERROR_REJECTED -> ActionFeedback(
                    success = false,
                    message = "Controlador rechazó el comando"
                )
            }
        }
    }

    fun clearActionResult() { _actionResult.value = null }

    // ── Emergency stop ─────────────────────────────────────
    /**
     * Desactiva TODOS los relés inmediatamente.
     * No requiere confirmación individual.
     */
    fun emergencyStopAll() {
        viewModelScope.launch {
            ML5_RELAYS.forEach { relay ->
                tcpService.writeCoil(relay.coilAddress, false)
                kotlinx.coroutines.delay(50)   // small gap between commands
            }
            _actionResult.value = ActionFeedback(
                success = true,
                message = "🛑 PARO DE EMERGENCIA — Todos los relés desactivados"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        tcpService.release()
    }
}

data class ActionFeedback(
    val success: Boolean,
    val message: String
)

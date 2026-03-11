package com.containerpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.containerpro.model.ElectricalData
import com.containerpro.model.RelayInfo
import com.containerpro.model.TemperatureData
import com.containerpro.service.protocol.ML5TcpService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceControlViewModel : ViewModel() {

    private val _serviceModeEnabled = MutableStateFlow(false)
    val serviceModeEnabled: StateFlow<Boolean> = _serviceModeEnabled

    private val _relays = MutableStateFlow(defaultRelays())
    val relays: StateFlow<List<RelayInfo>> = _relays

    private val _electrical = MutableStateFlow(ElectricalData())
    val electrical: StateFlow<ElectricalData> = _electrical

    private val _temperatures = MutableStateFlow(TemperatureData())
    val temperatures: StateFlow<TemperatureData> = _temperatures

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var tcpService: ML5TcpService? = null

    fun connect(ipAddress: String) {
        tcpService = ML5TcpService(ipAddress)
    }

    fun toggleServiceMode() {
        viewModelScope.launch {
            val newState = !_serviceModeEnabled.value
            val success = tcpService?.setServiceMode(newState) ?: true
            if (success) _serviceModeEnabled.value = newState
        }
    }

    fun toggleRelay(relayInfo: RelayInfo) {
        viewModelScope.launch {
            val newActive = !relayInfo.isActive
            val success = tcpService?.setRelay(relayInfo.address, newActive) ?: true
            if (success) {
                _relays.value = _relays.value.map {
                    if (it.address == relayInfo.address) it.copy(isActive = newActive) else it
                }
            }
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            tcpService?.emergencyStop()
            _relays.value = _relays.value.map { it.copy(isActive = false) }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val elec = tcpService?.readElectricalData()
                val temp = tcpService?.readTemperatureData()
                if (elec != null) _electrical.value = elec
                if (temp != null) _temperatures.value = temp
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { tcpService?.disconnect() }
    }

    private fun defaultRelays() = listOf(
        RelayInfo(0x0001, "Compresor",        "Compresor",   isDanger = true),
        RelayInfo(0x0002, "Desc. 1",          "Descargadores"),
        RelayInfo(0x0003, "Desc. 2",          "Descargadores"),
        RelayInfo(0x0004, "Fan Evap. 1",      "Ventilación"),
        RelayInfo(0x0005, "Fan Evap. 2",      "Ventilación"),
        RelayInfo(0x0006, "Fan Cond.",         "Ventilación"),
        RelayInfo(0x0007, "Calefactor Evap.", "Deshielo"),
        RelayInfo(0x0008, "Calefactor Dren.", "Deshielo"),
        RelayInfo(0x0009, "Sol. Líquido",     "Refrigerante"),
        RelayInfo(0x000A, "Sol. Suction",     "Refrigerante"),
        RelayInfo(0x000B, "Resistencia Aux.", "Auxiliar",    isDanger = true),
        RelayInfo(0x000C, "Alarma",           "Auxiliar"),
    )
}

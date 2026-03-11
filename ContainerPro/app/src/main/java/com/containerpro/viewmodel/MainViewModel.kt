package com.containerpro.viewmodel

import androidx.lifecycle.ViewModel
import com.containerpro.model.ContainerSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _currentSession = MutableStateFlow<ContainerSession?>(null)
    val currentSession: StateFlow<ContainerSession?> = _currentSession

    private val _wifiConnected = MutableStateFlow(false)
    val wifiConnected: StateFlow<Boolean> = _wifiConnected

    var groupOwnerIp: String? = null
        private set

    fun onContainerScanned(containerId: String) {
        _currentSession.value = ContainerSession(
            containerId  = containerId,
            modelName    = "ML5 OptimalINE",
            technicianId = "TE-001",
        )
    }

    fun onWifiConnected(ip: String) {
        _wifiConnected.value = true
        groupOwnerIp = ip
    }

    fun onWifiDisconnected() {
        _wifiConnected.value = false
        groupOwnerIp = null
    }
}

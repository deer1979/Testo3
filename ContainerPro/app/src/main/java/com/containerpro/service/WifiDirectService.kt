package com.containerpro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.*
import android.os.Looper
import com.containerpro.model.DeviceStatus
import com.containerpro.model.WifiDevice
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiDirectService(private val context: Context) {

    private val manager: WifiP2pManager? by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    }

    private var channel: WifiP2pManager.Channel? = null

    private val _state = MutableStateFlow(WifiDirectState())
    val state: StateFlow<WifiDirectState> = _state.asStateFlow()

    val peerChannel = Channel<List<WifiDevice>>(Channel.BUFFERED)

    private val intentFilter = IntentFilter().apply {
        addAction(WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(EXTRA_WIFI_STATE, -1)
                    _state.value = _state.value.copy(
                        isEnabled = state == WIFI_P2P_STATE_ENABLED
                    )
                }
                WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager?.requestPeers(channel) { peerList ->
                        val devices = peerList.deviceList.map { device ->
                            WifiDevice(
                                deviceName    = device.deviceName,
                                deviceAddress = device.deviceAddress,
                                status        = mapStatus(device.status)
                            )
                        }
                        // Filter to show only reefer units (name pattern)
                        val filtered = devices.filter { it.deviceName.isReeferUnit() }
                        _state.value = _state.value.copy(peers = filtered)
                    }
                }
                WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo = intent.getParcelableExtra<android.net.NetworkInfo>(
                        android.net.wifi.p2p.WifiP2pManager.EXTRA_NETWORK_INFO
                    )
                    if (networkInfo?.isConnected == true) {
                        manager?.requestConnectionInfo(channel) { info ->
                            _state.value = _state.value.copy(
                                isConnected   = true,
                                groupOwnerIp  = info.groupOwnerAddress?.hostAddress ?: "",
                                isGroupOwner  = info.isGroupOwner
                            )
                        }
                    } else {
                        _state.value = _state.value.copy(
                            isConnected  = false,
                            groupOwnerIp = ""
                        )
                    }
                }
                WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val device = intent.getParcelableExtra<WifiP2pDevice>(EXTRA_WIFI_P2P_DEVICE)
                    _state.value = _state.value.copy(thisDeviceName = device?.deviceName ?: "")
                }
            }
        }
    }

    fun initialize() {
        channel = manager?.initialize(context, Looper.getMainLooper(), null)
        context.registerReceiver(receiver, intentFilter)
    }

    fun release() {
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        channel?.close()
    }

    fun discoverPeers(
        onSuccess: () -> Unit = {},
        onFailure: (Int) -> Unit = {}
    ) {
        _state.value = _state.value.copy(isDiscovering = true, peers = emptyList())
        manager?.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                onSuccess()
            }
            override fun onFailure(reason: Int) {
                _state.value = _state.value.copy(isDiscovering = false)
                onFailure(reason)
            }
        })
    }

    fun stopDiscovery() {
        manager?.stopPeerDiscovery(channel, object : ActionListener {
            override fun onSuccess() { _state.value = _state.value.copy(isDiscovering = false) }
            override fun onFailure(reason: Int) { _state.value = _state.value.copy(isDiscovering = false) }
        })
    }

    fun connect(
        device  : WifiDevice,
        onSuccess: () -> Unit = {},
        onFailure: (Int) -> Unit = {}
    ) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup     = android.net.wifi.WpsInfo.PBC
        }
        _state.value = _state.value.copy(connectingTo = device.deviceName)
        manager?.connect(channel, config, object : ActionListener {
            override fun onSuccess()         { onSuccess() }
            override fun onFailure(reason: Int) {
                _state.value = _state.value.copy(connectingTo = null)
                onFailure(reason)
            }
        })
    }

    fun disconnect(onSuccess: () -> Unit = {}) {
        manager?.removeGroup(channel, object : ActionListener {
            override fun onSuccess() {
                _state.value = _state.value.copy(isConnected = false, connectingTo = null, groupOwnerIp = "")
                onSuccess()
            }
            override fun onFailure(reason: Int) {}
        })
    }

    private fun mapStatus(status: Int) = when (status) {
        WifiP2pDevice.AVAILABLE   -> DeviceStatus.AVAILABLE
        WifiP2pDevice.CONNECTED   -> DeviceStatus.CONNECTED
        WifiP2pDevice.INVITED     -> DeviceStatus.INVITED
        WifiP2pDevice.FAILED      -> DeviceStatus.FAILED
        else                      -> DeviceStatus.AVAILABLE
    }

    private fun String.isReeferUnit(): Boolean {
        val lower = lowercase()
        return lower.contains("ml5") || lower.contains("ml3") ||
               lower.contains("reefer") || lower.contains("ctd") ||
               lower.contains("carrier") || lower.contains("optima") ||
               lower.contains("natura") || lower.contains("prime") ||
               lower.contains("elite") || lower.contains("thin")
    }
}

data class WifiDirectState(
    val isEnabled      : Boolean          = false,
    val isDiscovering  : Boolean          = false,
    val isConnected    : Boolean          = false,
    val peers          : List<WifiDevice> = emptyList(),
    val connectingTo   : String?          = null,
    val groupOwnerIp   : String           = "",
    val isGroupOwner   : Boolean          = false,
    val thisDeviceName : String           = ""
)

// Broadcast receiver registered in manifest
class WifiDirectBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Handled per-instance by WifiDirectService
    }
}

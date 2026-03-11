package com.containerpro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager

/**
 * Lightweight WiFi Direct discovery wrapper.
 * The activity / ViewModel observes [peers] and [connectionInfo].
 */
class WifiDirectService(
    private val context : Context,
    private val manager : WifiP2pManager,
    private val channel : WifiP2pManager.Channel,
) {
    val peers = mutableListOf<WifiP2pDevice>()
    var groupOwnerAddress: String? = null

    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager.requestPeers(channel) { peerList ->
                        peers.clear()
                        peers.addAll(peerList.deviceList)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    manager.requestConnectionInfo(channel) { info ->
                        if (info.groupFormed && info.isGroupOwner.not()) {
                            groupOwnerAddress = info.groupOwnerAddress?.hostAddress
                        }
                    }
                }
            }
        }
    }

    fun discoverPeers(onSuccess: () -> Unit = {}, onFailure: (Int) -> Unit = {}) {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess()          = onSuccess()
            override fun onFailure(reason: Int) = onFailure(reason)
        })
    }

    fun stopDiscovery() {
        manager.stopPeerDiscovery(channel, null)
    }

    fun connect(device: WifiP2pDevice, onSuccess: () -> Unit = {}, onFailure: (Int) -> Unit = {}) {
        val config = android.net.wifi.p2p.WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess()          = onSuccess()
            override fun onFailure(reason: Int) = onFailure(reason)
        })
    }

    fun disconnect() {
        manager.removeGroup(channel, null)
    }
}

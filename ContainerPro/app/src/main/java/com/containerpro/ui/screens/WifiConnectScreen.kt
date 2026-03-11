package com.containerpro.ui.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.containerpro.model.*
import com.containerpro.ui.components.*
import com.containerpro.ui.theme.*
import com.containerpro.viewmodel.MainViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifiConnectScreen(navController: NavController, vm: MainViewModel = viewModel()) {

    val wifiState   by vm.wifiState.collectAsStateWithLifecycle()
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
    )

    LaunchedEffect(Unit) {
        if (!permissions.allPermissionsGranted) permissions.launchMultiplePermissionRequest()
    }

    Scaffold(
        topBar = {
            ProTopBar(
                title    = "Conexión WiFi Direct",
                subtitle = if (wifiState.isConnected) "Conectado" else
                           if (wifiState.isDiscovering) "Buscando equipos..." else "Desconectado",
                onBack   = { navController.popBackStack() }
            )
        },
        containerColor = Neutral10
    ) { pad ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // ── Connection status card ─────────────────────────
            item {
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = if (wifiState.isConnected) AppColors.tagGreen else NeutralVar20,
                    border = BorderStroke(1.dp,
                        if (wifiState.isConnected) Emerald40.copy(alpha = 0.5f)
                        else AppColors.divider
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "wifi")
                        val wifiAlpha by infiniteTransition.animateFloat(
                            0.4f, 1f,
                            infiniteRepeatable(tween(800), RepeatMode.Reverse),
                            label = "alpha"
                        )
                        Icon(
                            if (wifiState.isConnected) Icons.Rounded.WifiTethering
                            else Icons.Rounded.WifiOff,
                            null,
                            tint = if (wifiState.isConnected) AppColors.success
                                   else NeutralVar50,
                            modifier = Modifier
                                .size(36.dp)
                                .alpha(if (wifiState.isConnected) wifiAlpha else 1f)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (wifiState.isConnected) "Conectado al Equipo"
                                else "Sin Conexión",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (wifiState.isConnected) AppColors.success
                                        else Neutral80
                            )
                            Text(
                                if (wifiState.isConnected)
                                    "IP: ${wifiState.groupOwnerIp}  ·  ${wifiState.connectingTo ?: ""}"
                                else "Escanee para encontrar unidades ML3/ML5",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralVar60
                            )
                        }
                    }
                }
            }

            // ── Action buttons ────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!wifiState.isConnected) {
                        ProButton(
                            text    = if (wifiState.isDiscovering) "Buscando..." else "Buscar Equipos",
                            onClick = { vm.discoverWifiPeers() },
                            icon    = if (wifiState.isDiscovering) null else Icons.Rounded.Search,
                            loading = wifiState.isDiscovering,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        ProButton(
                            text     = "Desconectar",
                            onClick  = { vm.disconnectWifi() },
                            variant  = ButtonVariant.DESTRUCTIVE,
                            icon     = Icons.Rounded.WifiOff,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (wifiState.isDiscovering) {
                        ProButton(
                            text     = "Detener",
                            onClick  = { vm.stopDiscovery() },
                            variant  = ButtonVariant.SECONDARY,
                            icon     = Icons.Rounded.Stop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Peers list ────────────────────────────────────
            if (wifiState.peers.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader("Equipos Encontrados — ${wifiState.peers.size}")
                }

                items(wifiState.peers) { device ->
                    WifiDeviceCard(
                        device      = device,
                        isConnecting = wifiState.connectingTo == device.deviceName,
                        onConnect   = { vm.connectToDevice(device) }
                    )
                }
            } else if (!wifiState.isDiscovering && !wifiState.isConnected) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.WifiFind, null,
                            tint = NeutralVar40, modifier = Modifier.size(56.dp))
                        Text("Ningún equipo encontrado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralVar50)
                        Text("Asegúrese que la unidad ML5 esté\nencendida con WiFi activo",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralVar40,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            // Tip
            item {
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = AppColors.tagAmber,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Rounded.Lightbulb, null,
                            tint = AppColors.warning, modifier = Modifier.size(18.dp))
                        Text(
                            "Solo unidades ML5 (NaturaLine, OptimalINE, PrimeLine) " +
                            "soportan conexión WiFi Direct. Las unidades ML3 usan DataLine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber90
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun WifiDeviceCard(
    device      : WifiDevice,
    isConnecting: Boolean,
    onConnect   : () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = AppColors.cardSurface),
        border   = BorderStroke(1.dp, when (device.status) {
            DeviceStatus.CONNECTED -> Emerald40.copy(0.5f)
            DeviceStatus.INVITED   -> Amber40.copy(0.5f)
            else                   -> AppColors.divider
        })
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Signal icon
            Box(
                Modifier
                    .size(44.dp)
                    .background(
                        when (device.status) {
                            DeviceStatus.CONNECTED -> AppColors.tagGreen
                            else -> NeutralVar20
                        },
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Router,
                    null,
                    tint = when (device.status) {
                        DeviceStatus.CONNECTED -> AppColors.success
                        else -> NeutralVar50
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(Modifier.weight(1f)) {
                Text(device.deviceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium)
                Text(device.deviceAddress,
                    style = codeSmallStyle,
                    color = NeutralVar60,
                    fontSize = 11.sp)
                Text(
                    when (device.status) {
                        DeviceStatus.CONNECTED  -> "✓ Conectado"
                        DeviceStatus.CONNECTING -> "Conectando..."
                        DeviceStatus.INVITED    -> "Invitado"
                        DeviceStatus.FAILED     -> "Falló"
                        DeviceStatus.AVAILABLE  -> "Disponible"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (device.status) {
                        DeviceStatus.CONNECTED -> AppColors.success
                        DeviceStatus.FAILED    -> AppColors.error
                        else                   -> NeutralVar60
                    }
                )
            }

            if (device.status != DeviceStatus.CONNECTED) {
                FilledTonalButton(
                    onClick  = onConnect,
                    enabled  = !isConnecting,
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Emerald30
                    )
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp, color = Emerald80)
                    } else {
                        Text("Conectar",
                            style = MaterialTheme.typography.labelMedium,
                            color = Emerald80)
                    }
                }
            }
        }
    }
}

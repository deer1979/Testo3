package com.containerpro.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.navigation.Screen
import com.containerpro.ui.theme.SuccessGreen
import com.containerpro.ui.theme.WarningAmber
import com.containerpro.viewmodel.MainViewModel

// Simulated device data for UI demo
data class MockDevice(val name: String, val address: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiConnectScreen(
    navController: NavController,
    vm           : MainViewModel = viewModel(),
) {
    val wifiConnected by vm.wifiConnected.collectAsState()
    var isSearching   by remember { mutableStateOf(false) }
    var devices       by remember { mutableStateOf(emptyList<MockDevice>()) }

    // Simulate discovery
    LaunchedEffect(isSearching) {
        if (isSearching) {
            kotlinx.coroutines.delay(2000)
            devices = listOf(
                MockDevice("ML5-OptimalINE-A7F3", "192.168.49.1"),
                MockDevice("ML5-NaturaLine-B2C1", "192.168.49.2"),
            )
            isSearching = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wifi_connect_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (wifiConnected) SuccessGreen.copy(0.4f) else WarningAmber.copy(0.3f),
                        RoundedCornerShape(20.dp),
                    ),
                shape = RoundedCornerShape(20.dp),
                color = if (wifiConnected) SuccessGreen.copy(0.08f) else WarningAmber.copy(0.06f),
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = if (wifiConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                        contentDescription = null,
                        tint = if (wifiConnected) SuccessGreen else WarningAmber,
                        modifier = Modifier.size(28.dp),
                    )
                    Column {
                        Text(
                            if (wifiConnected) stringResource(R.string.connected_to) else stringResource(R.string.no_connection),
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                        )
                        if (wifiConnected && vm.groupOwnerIp != null) {
                            Text(vm.groupOwnerIp!!, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (wifiConnected) {
                        TextButton(onClick = { vm.onWifiDisconnected() }) {
                            Text(stringResource(R.string.disconnect), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Search button
            Button(
                onClick  = { if (isSearching) isSearching = false else { devices = emptyList(); isSearching = true } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
            ) {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.searching))
                } else {
                    Icon(Icons.Rounded.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.search_devices))
                }
            }

            // Device list
            if (devices.isNotEmpty()) {
                Text(
                    "EQUIPOS ENCONTRADOS",
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(devices) { device ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Row(
                                modifier          = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(Icons.Rounded.Router, null, tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(device.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(device.address, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = {
                                        vm.onWifiConnected(device.address)
                                        navController.navigate(Screen.ServiceControl.createRoute(device.address)) {
                                            popUpTo(Screen.WifiConnect.route)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(stringResource(R.string.connect))
                                }
                            }
                        }
                    }
                }
            } else if (!isSearching) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.WifiFind, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp))
                        Text(stringResource(R.string.no_devices_found), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Info tip
            Spacer(Modifier.weight(1f))
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                shape    = RoundedCornerShape(14.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.ml5_tip), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

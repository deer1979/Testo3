package com.containerpro.ui.screens.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.model.RelayInfo
import com.containerpro.ui.theme.*
import com.containerpro.viewmodel.ServiceControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceControlScreen(
    navController: NavController,
    ipAddress    : String,
    vm           : ServiceControlViewModel = viewModel(),
) {
    // Connect on enter
    LaunchedEffect(ipAddress) { vm.connect(ipAddress) }

    val relays          by vm.relays.collectAsState()
    val electrical      by vm.electrical.collectAsState()
    val temperatures    by vm.temperatures.collectAsState()
    val serviceMode     by vm.serviceModeEnabled.collectAsState()
    val isLoading       by vm.isLoading.collectAsState()
    val errorMessage    by vm.errorMessage.collectAsState()

    var showEmergencyDialog    by remember { mutableStateOf(false) }
    var pendingRelay           by remember { mutableStateOf<RelayInfo?>(null) }
    var showDangerConfirm      by remember { mutableStateOf(false) }

    // Group relays
    val relayGroups = relays.groupBy { it.group }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.relay_control),
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            ipAddress,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null)
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    IconButton(onClick = { vm.refreshData() }) {
                        Icon(Icons.Rounded.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            // EMERGENCY STOP
            Surface(
                color     = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick  = { showEmergencyDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(56.dp),
                    shape  = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorCoral,
                        contentColor   = Color.White,
                    ),
                ) {
                    Icon(Icons.Rounded.PowerOff, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.emergency_stop).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Error banner ───────────────────────────────
            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        color    = ErrorCoral.copy(alpha = 0.12f),
                    ) {
                        Row(
                            modifier          = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Rounded.Warning, null, tint = ErrorCoral)
                            Text(errorMessage!!, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // ── Service mode toggle ────────────────────────
            item {
                ServiceModeCard(
                    enabled  = serviceMode,
                    onToggle = { vm.toggleServiceMode() },
                )
            }

            // ── Relay groups ───────────────────────────────
            relayGroups.forEach { (groupName, groupRelays) ->
                item {
                    Text(
                        groupName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.9.sp,
                    )
                }
                items(groupRelays) { relay ->
                    RelayToggleCard(
                        relay   = relay,
                        enabled = serviceMode,
                        onToggle = {
                            if (relay.isDanger) {
                                pendingRelay = relay
                                showDangerConfirm = true
                            } else {
                                vm.toggleRelay(relay)
                            }
                        },
                    )
                }
            }

            // ── Live electrical summary ────────────────────
            item {
                Text(
                    stringResource(R.string.electrical_data).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.9.sp,
                )
            }
            item {
                ElectricalSummaryCard(
                    voltages  = listOf(electrical.voltageL1, electrical.voltageL2, electrical.voltageL3),
                    currentComp = electrical.currentComp,
                    powerKw   = electrical.powerKw,
                    hz        = electrical.frequencyHz,
                )
            }

            // ── Live temperature summary ───────────────────
            item {
                Text(
                    stringResource(R.string.temperatures).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.9.sp,
                )
            }
            item {
                TempSummaryCard(
                    supply  = temperatures.supplyAir,
                    ret     = temperatures.returnAir,
                    ambient = temperatures.ambient,
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    // ── Emergency stop confirmation dialog ─────────────────
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            icon  = { Icon(Icons.Rounded.Warning, null, tint = ErrorCoral, modifier = Modifier.size(36.dp)) },
            title = { Text(stringResource(R.string.emergency_stop), fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.emergency_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = { vm.emergencyStop(); showEmergencyDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = ErrorCoral),
                ) { Text(stringResource(R.string.confirm).uppercase(), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    // ── Danger relay confirmation ──────────────────────────
    if (showDangerConfirm && pendingRelay != null) {
        AlertDialog(
            onDismissRequest = { showDangerConfirm = false; pendingRelay = null },
            icon  = { Icon(Icons.Rounded.ElectricBolt, null, tint = WarningAmber, modifier = Modifier.size(30.dp)) },
            title = { Text("${pendingRelay!!.name}", fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.danger_relay_confirm)) },
            confirmButton = {
                Button(onClick = {
                    pendingRelay?.let { vm.toggleRelay(it) }
                    showDangerConfirm = false
                    pendingRelay = null
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDangerConfirm = false; pendingRelay = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

// ── Service mode card ──────────────────────────────────────
@Composable
private fun ServiceModeCard(enabled: Boolean, onToggle: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (enabled) Amber20.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(400),
        label = "serviceBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (enabled) Amber70.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
        animationSpec = tween(400),
        label = "serviceBorder",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = if (enabled) Icons.Rounded.Engineering else Icons.Rounded.Lock,
                contentDescription = null,
                tint     = if (enabled) Amber70 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.service_mode),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (enabled) stringResource(R.string.service_mode_on_desc)
                    else         stringResource(R.string.service_mode_off_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked  = enabled,
                onCheckedChange = { onToggle() },
                colors   = SwitchDefaults.colors(
                    checkedThumbColor  = Amber70,
                    checkedTrackColor  = Amber70.copy(alpha = 0.3f),
                ),
            )
        }
    }
}

// ── Relay toggle card ──────────────────────────────────────
@Composable
private fun RelayToggleCard(relay: RelayInfo, enabled: Boolean, onToggle: () -> Unit) {
    val dotColor by animateColorAsState(
        targetValue = if (relay.isActive) SuccessGreen else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "dot",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(6.dp),
                ) {
                    Text(relay.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (relay.isDanger) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningAmber.copy(alpha = 0.15f),
                        ) {
                            Text(
                                "⚡",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    "0x%04X".format(relay.address),
                    style  = MaterialTheme.typography.labelSmall,
                    color  = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                if (relay.isActive) stringResource(R.string.on) else stringResource(R.string.off),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (relay.isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Switch(
                checked         = relay.isActive,
                onCheckedChange = { if (enabled) onToggle() },
                enabled         = enabled,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor  = SuccessGreen,
                    checkedTrackColor  = SuccessGreen.copy(alpha = 0.25f),
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.outline,
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                ),
            )
        }
    }
}

// ── Electrical summary ─────────────────────────────────────
@Composable
private fun ElectricalSummaryCard(
    voltages  : List<Float>,
    currentComp: Float,
    powerKw   : Float,
    hz        : Float,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Phase voltages
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("L1", "L2", "L3").zip(voltages).forEach { (label, v) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ) {
                        Column(
                            modifier           = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("%.0f V".format(v),
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair("A comp.", "%.1f A".format(currentComp)),
                    Pair("kW",      "%.1f kW".format(powerKw)),
                    Pair("Hz",      "%.1f Hz".format(hz)),
                ).forEach { (lbl, value) ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(lbl, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ── Temperature summary ────────────────────────────────────
@Composable
private fun TempSummaryCard(supply: Float, ret: Float, ambient: Float) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf(
                Triple("Supply", supply, "°C"),
                Triple("Return", ret,    "°C"),
                Triple("Amb.",  ambient, "°C"),
            ).forEach { (lbl, value, unit) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%.1f%s".format(value, unit),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Text(lbl, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

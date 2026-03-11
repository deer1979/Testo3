package com.containerpro.ui.screens.control

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.containerpro.service.protocol.*
import com.containerpro.ui.components.*
import com.containerpro.ui.theme.*
import com.containerpro.viewmodel.ServiceControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceControlScreen(
    navController : NavController,
    unitIpAddress : String,
    vm            : ServiceControlViewModel = viewModel()
) {
    val tcpState        by vm.connectionState.collectAsStateWithLifecycle()
    val electricalData  by vm.electricalData.collectAsStateWithLifecycle()
    val coilStates      by vm.coilStates.collectAsStateWithLifecycle()
    val serviceMode     by vm.serviceModeActive.collectAsStateWithLifecycle()
    val pendingRelay    by vm.pendingRelay.collectAsStateWithLifecycle()
    val actionResult    by vm.actionResult.collectAsStateWithLifecycle()
    val isConnecting    by vm.isConnecting.collectAsStateWithLifecycle()
    val lastError       by vm.lastError.collectAsStateWithLifecycle()

    // Auto-connect on entry
    LaunchedEffect(unitIpAddress) {
        if (tcpState == TcpConnectionState.DISCONNECTED) {
            vm.connectToUnit(unitIpAddress)
        }
    }

    // Snackbar for action feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionResult) {
        actionResult?.let {
            snackbarHostState.showSnackbar(
                message  = it.message,
                duration = SnackbarDuration.Short
            )
            vm.clearActionResult()
        }
    }

    // Confirmation dialog
    pendingRelay?.let { relay ->
        val enable = vm.pendingAction.collectAsStateWithLifecycle().value
        RelayConfirmDialog(
            relay    = relay,
            enable   = enable,
            onConfirm = { vm.confirmRelayToggle() },
            onDismiss = { vm.cancelPendingRelay() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Control Manual — ML5",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text(unitIpAddress,
                            style = codeSmallStyle.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.disconnectFromUnit()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    // Emergency stop
                    if (serviceMode) {
                        IconButton(onClick = { vm.emergencyStopAll() }) {
                            Icon(Icons.Rounded.PowerOff, null, tint = AppColors.error,
                                modifier = Modifier.size(26.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardSurface)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isSuccess = !data.visuals.message.contains("Error") &&
                                !data.visuals.message.contains("No se") &&
                                !data.visuals.message.contains("Sin respuesta")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSuccess) AppColors.tagGreen else AppColors.tagRed,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                            null, tint = if (isSuccess) AppColors.success else AppColors.error,
                            modifier = Modifier.size(18.dp))
                        Text(data.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSuccess) Emerald90 else Coral90,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        containerColor = Neutral10
    ) { pad ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {

            // ── TCP Connection status ──────────────────────
            item { TcpStatusBanner(tcpState, isConnecting, lastError, unitIpAddress,
                onRetry = { vm.connectToUnit(unitIpAddress) }) }

            // ── Service mode toggle ────────────────────────
            item {
                ServiceModeCard(
                    serviceMode     = serviceMode,
                    tcpConnected    = tcpState == TcpConnectionState.CONNECTED,
                    onEnable        = { vm.activateServiceMode() },
                    onDisable       = { vm.deactivateServiceMode() },
                    onEmergencyStop = { vm.emergencyStopAll() }
                )
            }

            // ── Electrical readings ────────────────────────
            electricalData?.let { data ->
                item {
                    SectionHeader("Datos Eléctricos — Tiempo Real")
                }
                item { VoltageSection(data) }
                item { CurrentSection(data) }
                item { PowerSection(data) }
                item { TemperatureSection(data) }
            }

            if (tcpState == TcpConnectionState.CONNECTED && electricalData == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp),
                                strokeWidth = 2.dp, color = Emerald60)
                            Text("Leyendo parámetros...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeutralVar60)
                        }
                    }
                }
            }

            // ── Relay control groups ───────────────────────
            if (serviceMode) {
                val grouped = ML5_RELAYS.groupBy { it.group }
                grouped.forEach { (group, relays) ->
                    item {
                        Spacer(Modifier.height(4.dp))
                        SectionHeader(group.label)
                    }
                    item {
                        RelayGroupCard(
                            relays     = relays,
                            coilStates = coilStates,
                            onToggle   = { relay, enable -> vm.requestRelayToggle(relay, enable) }
                        )
                    }
                }
            } else if (tcpState == TcpConnectionState.CONNECTED) {
                item {
                    Surface(
                        shape  = RoundedCornerShape(16.dp),
                        color  = AppColors.tagAmber,
                        border = BorderStroke(1.dp, Amber40.copy(0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Lock, null,
                                tint = AppColors.warning, modifier = Modifier.size(22.dp))
                            Column {
                                Text("Modo Servicio requerido",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold, color = Amber90)
                                Text("Active el Modo Servicio para controlar los relés manualmente.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Amber80)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ── TCP Status Banner ──────────────────────────────────────
@Composable
private fun TcpStatusBanner(
    state      : TcpConnectionState,
    loading    : Boolean,
    error      : String?,
    ip         : String,
    onRetry    : () -> Unit
) {
    val (bg, fg, icon, label) = when (state) {
        TcpConnectionState.CONNECTED    ->
            arrayOf(AppColors.tagGreen, AppColors.success,
                Icons.Rounded.WifiTethering, "TCP Conectado — Puerto ${ML5Protocol.DEFAULT_PORT}")
        TcpConnectionState.CONNECTING   ->
            arrayOf(AppColors.tagAmber, AppColors.warning,
                Icons.Rounded.Wifi, "Conectando...")
        TcpConnectionState.ERROR        ->
            arrayOf(AppColors.tagRed, AppColors.error,
                Icons.Rounded.WifiOff, error ?: "Error de conexión")
        TcpConnectionState.DISCONNECTED ->
            arrayOf(NeutralVar20, NeutralVar60,
                Icons.Rounded.WifiOff, "Desconectado")
    }

    Surface(
        shape  = RoundedCornerShape(14.dp),
        color  = bg as Color,
        border = BorderStroke(1.dp, (fg as Color).copy(0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val pulse = rememberInfiniteTransition(label = "p")
            val a by pulse.animateFloat(0.4f, 1f,
                infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
            Icon(icon as androidx.compose.ui.graphics.vector.ImageVector, null,
                tint = fg.copy(alpha = if (state == TcpConnectionState.CONNECTING) a else 1f),
                modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f)) {
                Text(label as String,
                    style = MaterialTheme.typography.labelMedium,
                    color = fg, fontWeight = FontWeight.SemiBold)
                Text(ip,
                    style = codeSmallStyle.copy(fontSize = 11.sp),
                    color = (fg as Color).copy(0.7f))
            }
            if (state == TcpConnectionState.ERROR || state == TcpConnectionState.DISCONNECTED) {
                FilledTonalButton(
                    onClick = onRetry,
                    enabled = !loading,
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Emerald30)
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(14.dp),
                        strokeWidth = 2.dp, color = Emerald80)
                    else Text("Reintentar",
                        style = MaterialTheme.typography.labelSmall, color = Emerald80)
                }
            }
        }
    }
}

// ── Service Mode Card ──────────────────────────────────────
@Composable
private fun ServiceModeCard(
    serviceMode     : Boolean,
    tcpConnected    : Boolean,
    onEnable        : () -> Unit,
    onDisable       : () -> Unit,
    onEmergencyStop : () -> Unit
) {
    Surface(
        shape  = RoundedCornerShape(18.dp),
        color  = if (serviceMode) Emerald30 else AppColors.cardSurface,
        border = BorderStroke(1.5.dp,
            if (serviceMode) Emerald60.copy(0.6f) else AppColors.divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(44.dp).background(
                        if (serviceMode) Emerald40.copy(0.35f) else NeutralVar20,
                        RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (serviceMode) Icons.Rounded.Build else Icons.Rounded.BuildCircle,
                        null,
                        tint = if (serviceMode) Emerald70 else NeutralVar50,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Modo Servicio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (serviceMode) Emerald90 else Neutral90)
                    Text(if (serviceMode) "Control manual habilitado — Actúe con precaución"
                         else "Habilitar para controlar relés manualmente",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (serviceMode) Emerald70 else NeutralVar60)
                }
                Switch(
                    checked  = serviceMode,
                    onCheckedChange = { if (it) onEnable() else onDisable() },
                    enabled  = tcpConnected,
                    colors   = SwitchDefaults.colors(
                        checkedThumbColor      = Neutral99,
                        checkedTrackColor      = Emerald50,
                        uncheckedThumbColor    = NeutralVar50,
                        uncheckedTrackColor    = NeutralVar30
                    )
                )
            }
            if (serviceMode) {
                // Emergency stop button
                Button(
                    onClick  = onEmergencyStop,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1F1F))
                ) {
                    Icon(Icons.Rounded.PowerOff, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("PARO DE EMERGENCIA — Desactivar Todo",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Voltage Section ────────────────────────────────────────
@Composable
private fun VoltageSection(data: ElectricalData) {
    Surface(shape = RoundedCornerShape(16.dp), color = AppColors.cardSurface,
        border = BorderStroke(1.dp, AppColors.divider)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.ElectricBolt, null,
                    tint = Amber70, modifier = Modifier.size(18.dp))
                Text("Voltaje de Red",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                // Voltage imbalance indicator
                val imbalance = data.voltageImbalance
                val imbalanceColor = when {
                    imbalance < 2f -> AppColors.success
                    imbalance < 5f -> AppColors.warning
                    else           -> AppColors.error
                }
                Surface(shape = RoundedCornerShape(6.dp),
                    color = imbalanceColor.copy(0.14f)) {
                    Text("Δ ${"%.1f".format(imbalance)}%",
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = imbalanceColor, fontWeight = FontWeight.Bold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VoltPhaseCard("L1", data.voltageL1, Modifier.weight(1f))
                VoltPhaseCard("L2", data.voltageL2, Modifier.weight(1f))
                VoltPhaseCard("L3", data.voltageL3, Modifier.weight(1f))
            }
            // Voltage bar
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(data.voltageL1, data.voltageL2, data.voltageL3).forEachIndexed { i, v ->
                    val pct = (v / 240f).coerceIn(0f, 1f)
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(6.dp)
                            .clip(RoundedCornerShape(3.dp)).background(NeutralVar30)) {
                            Box(Modifier.fillMaxWidth(pct).fillMaxHeight()
                                .background(Brush.horizontalGradient(
                                    listOf(Amber40, Amber70)), RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoltPhaseCard(phase: String, value: Float, modifier: Modifier) {
    val color = when {
        value < 190f || value > 250f -> AppColors.error
        value < 205f || value > 240f -> AppColors.warning
        else                          -> AppColors.success
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(phase, style = MaterialTheme.typography.labelSmall, color = NeutralVar60)
        Text("${"%.1f".format(value)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, color = color)
        Text("V", style = MaterialTheme.typography.labelSmall, color = NeutralVar50)
    }
}

// ── Current Section ────────────────────────────────────────
@Composable
private fun CurrentSection(data: ElectricalData) {
    Surface(shape = RoundedCornerShape(16.dp), color = AppColors.cardSurface,
        border = BorderStroke(1.dp, AppColors.divider)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.FlashOn, null, tint = Coral80, modifier = Modifier.size(18.dp))
                Text("Amperaje", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrentMeter("Compresor", data.currentCompressor, 30f, Coral80, Modifier.weight(1f))
                CurrentMeter("Fan Evap.",  data.currentEvapFan,    5f,  Emerald60, Modifier.weight(1f))
                CurrentMeter("Fan Cond.",  data.currentCondFan,    4f,  Amber70,   Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CurrentMeter(label: String, value: Float, max: Float, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralVar60,
            textAlign = TextAlign.Center)
        Text("${"%.1f".format(value)} A",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = color)
        Box(Modifier.fillMaxWidth().height(4.dp)
            .clip(RoundedCornerShape(2.dp)).background(NeutralVar30)) {
            Box(Modifier.fillMaxWidth((value / max).coerceIn(0f, 1f))
                .fillMaxHeight().background(color, RoundedCornerShape(2.dp)))
        }
    }
}

// ── Power Section ──────────────────────────────────────────
@Composable
private fun PowerSection(data: ElectricalData) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard("Potencia Total", "${"%.2f".format(data.powerTotal)}", "kW",
            Icons.Rounded.BoltBlue, Coral80, Modifier.weight(1f))
        MetricCard("Frecuencia", "${"%.1f".format(data.frequency)}", "Hz",
            Icons.Rounded.Waves, Emerald60, Modifier.weight(1f))
        MetricCard("F. Potencia", "${"%.2f".format(data.powerFactor)}", "",
            Icons.Rounded.Speed, Amber70, Modifier.weight(1f))
    }
}

// ── Temperature Section ────────────────────────────────────
@Composable
private fun TemperatureSection(data: ElectricalData) {
    Surface(shape = RoundedCornerShape(16.dp), color = AppColors.cardSurface,
        border = BorderStroke(1.dp, AppColors.divider)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Thermostat, null, tint = Emerald60, modifier = Modifier.size(18.dp))
                Text("Temperaturas Internas", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
            }
            val temps = listOf(
                Triple("Descarga Comp.", data.dischargeTemp, 130f),
                Triple("Succión Comp.",  data.suctionTemp,    20f),
                Triple("Bob. Evap.",     data.evapCoilTemp,    5f),
                Triple("Bob. Cond.",     data.condenserCoilTemp, 65f),
                Triple("Exterior",       data.ambientTemp,    50f),
            )
            temps.forEach { (label, value, warnThreshold) ->
                TempRow(label, value, warnThreshold)
            }
        }
    }
}

@Composable
private fun TempRow(label: String, value: Float, warnAt: Float) {
    val color = when {
        value > warnAt * 1.1f -> AppColors.error
        value > warnAt        -> AppColors.warning
        else                   -> NeutralVar70
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = NeutralVar60)
        Text("${"%.1f".format(value)} °C",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold, color = color)
    }
    HorizontalDivider(color = AppColors.divider.copy(0.4f), thickness = 0.5.dp)
}

// ── Relay Group Card ───────────────────────────────────────
@Composable
private fun RelayGroupCard(
    relays     : List<RelayDefinition>,
    coilStates : Map<Int, Boolean>,
    onToggle   : (RelayDefinition, Boolean) -> Unit
) {
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = AppColors.cardSurface,
        border = BorderStroke(1.dp, AppColors.divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(4.dp)) {
            relays.forEachIndexed { index, relay ->
                RelayRow(
                    relay      = relay,
                    isActive   = coilStates[relay.coilAddress] ?: false,
                    onToggle   = { enable -> onToggle(relay, enable) }
                )
                if (index < relays.lastIndex) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 12.dp),
                        color = AppColors.divider.copy(0.5f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun RelayRow(
    relay    : RelayDefinition,
    isActive : Boolean,
    onToggle : (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isActive) Emerald30.copy(alpha = 0.25f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status dot
        Box(
            Modifier.size(10.dp).background(
                if (isActive) AppColors.success else NeutralVar40, CircleShape)
        )

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(relay.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) Emerald80 else Neutral90)
                if (relay.isDangerous) {
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = AppColors.tagRed) {
                        Text("⚠",
                            Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.error)
                    }
                }
            }
            Text(relay.description,
                style = MaterialTheme.typography.labelSmall,
                color = NeutralVar60)
        }

        // ON/OFF buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // OFF
            FilledTonalButton(
                onClick  = { onToggle(false) },
                enabled  = isActive,
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors   = ButtonDefaults.filledTonalButtonColors(
                    containerColor         = AppColors.tagRed,
                    disabledContainerColor = NeutralVar20
                )
            ) {
                Text("OFF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) AppColors.error else NeutralVar50)
            }
            // ON
            FilledTonalButton(
                onClick  = { onToggle(true) },
                enabled  = !isActive,
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors   = ButtonDefaults.filledTonalButtonColors(
                    containerColor         = AppColors.tagGreen,
                    disabledContainerColor = NeutralVar20
                )
            ) {
                Text("ON",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!isActive) AppColors.success else NeutralVar50)
            }
        }
    }
}

// ── Confirmation Dialog ────────────────────────────────────
@Composable
private fun RelayConfirmDialog(
    relay     : RelayDefinition,
    enable    : Boolean,
    onConfirm : () -> Unit,
    onDismiss : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = NeutralVar20,
        shape            = RoundedCornerShape(20.dp),
        icon             = {
            Icon(
                if (relay.isDangerous) Icons.Rounded.Warning else Icons.Rounded.ElectricBolt,
                null,
                tint = if (relay.isDangerous) AppColors.warning else Emerald60,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                if (enable) "Activar ${relay.displayName}"
                else "Desactivar ${relay.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(relay.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralVar70, textAlign = TextAlign.Center)
                if (relay.isDangerous) {
                    Surface(shape = RoundedCornerShape(8.dp), color = AppColors.tagRed) {
                        Text("⚠ Componente de alto voltaje — Confirme antes de proceder",
                            Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.error, textAlign = TextAlign.Center)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (enable) Emerald40 else AppColors.tagRed.copy(1f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (enable) "Activar" else "Desactivar",
                    fontWeight = FontWeight.Bold,
                    color = if (enable) Neutral99 else AppColors.error)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, NeutralVar40)) {
                Text("Cancelar", color = NeutralVar70)
            }
        }
    )
}

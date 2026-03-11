package com.containerpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.containerpro.model.*
import com.containerpro.ui.components.*
import com.containerpro.ui.theme.*
import com.containerpro.viewmodel.MainViewModel

@Composable
fun DRRScreen(navController: NavController, vm: MainViewModel = viewModel()) {

    val drrData  by vm.drrData.collectAsStateWithLifecycle()
    val loading  by vm.drrLoading.collectAsStateWithLifecycle()
    val wifiState by vm.wifiState.collectAsStateWithLifecycle()
    val container by vm.scannedContainer.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (drrData == null && wifiState.isConnected) vm.fetchDRRData()
    }

    Scaffold(
        topBar = {
            ProTopBar(
                title    = "DRR — Diagnóstico",
                subtitle = container?.number ?: wifiState.connectingTo ?: "",
                onBack   = { navController.popBackStack() },
                actions  = {
                    IconButton(onClick = { vm.fetchDRRData() }, enabled = !loading) {
                        if (loading) CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary)
                        else Icon(Icons.Rounded.Refresh, null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        containerColor = Neutral10
    ) { pad ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Connection badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (wifiState.isConnected) AppColors.tagGreen else AppColors.tagRed
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusDot(wifiState.isConnected)
                    Text(
                        if (wifiState.isConnected) "WiFi Conectado — ${wifiState.groupOwnerIp}"
                        else "Sin conexión WiFi",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (wifiState.isConnected) AppColors.success else AppColors.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (loading && drrData == null) {
                Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("Leyendo parámetros del equipo...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralVar60)
                    }
                }
            }

            drrData?.let { data ->
                // ── Temperature section ─────────────────────────
                SectionHeader("Temperatura")
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        label = "Setpoint",
                        value = "%.1f".format(data.setPoint),
                        unit  = "°C",
                        icon  = Icons.Rounded.Thermostat,
                        tint  = Amber70,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Supply Air",
                        value = "%.1f".format(data.supplyAirTemp),
                        unit  = "°C",
                        icon  = Icons.Rounded.AcUnit,
                        tint  = Emerald60,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Return Air",
                        value = "%.1f".format(data.returnAirTemp),
                        unit  = "°C",
                        icon  = Icons.Rounded.Air,
                        tint  = Emerald60,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Temp deviation indicator
                val deviation = data.returnAirTemp - data.setPoint
                val devColor = when {
                    kotlin.math.abs(deviation) < 0.5f -> AppColors.success
                    kotlin.math.abs(deviation) < 1.5f -> AppColors.warning
                    else                              -> AppColors.error
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = devColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, devColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (kotlin.math.abs(deviation) < 1f) Icons.Rounded.CheckCircle
                            else Icons.Rounded.Warning,
                            null, tint = devColor, modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Desviación: ${if (deviation >= 0) "+" else ""}${"%.2f".format(deviation)} °C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = devColor, fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── System status ───────────────────────────────
                SectionHeader("Estado del Sistema")
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemStatusChip(
                        "Compresor",
                        data.compressorOn,
                        Icons.Rounded.ElectricBolt,
                        Modifier.weight(1f)
                    )
                    SystemStatusChip(
                        "Deshielo",
                        data.defrostMode,
                        Icons.Rounded.WaterDrop,
                        Modifier.weight(1f)
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        label = "Fan Evap.",
                        value = data.evaporatorFan.name,
                        icon  = Icons.Rounded.Air,
                        tint  = Emerald60,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Fan Cond.",
                        value = data.condenserFan.name,
                        icon  = Icons.Rounded.Air,
                        tint  = Amber70,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Consumo",
                        value = "%.1f".format(data.powerConsumption),
                        unit  = "kWh",
                        icon  = Icons.Rounded.BoltBlue,
                        tint  = Coral80,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Optional sensors ────────────────────────────
                if (data.humidity != null || data.co2Level != null) {
                    SectionHeader("Sensores Opcionales")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.humidity?.let {
                            MetricCard("Humedad", "%.0f".format(it), "%",
                                Icons.Rounded.WaterDrop, InfoTeal, Modifier.weight(1f))
                        }
                        data.co2Level?.let {
                            MetricCard("CO₂", "%.0f".format(it), "ppm",
                                Icons.Rounded.Air, NeutralVar60, Modifier.weight(1f))
                        }
                    }
                }

                // ── Alarms ──────────────────────────────────────
                SectionHeader(
                    "Alarmas Activas",
                    "${data.activeAlarms.size}"
                )
                if (data.activeAlarms.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.tagGreen
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null,
                                tint = AppColors.success, modifier = Modifier.size(20.dp))
                            Text("Sin alarmas activas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.success,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    data.activeAlarms.forEach { alarm ->
                        AlarmChip(alarm.severity, "${alarm.code} — ${alarm.description}")
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Última lectura: ${data.lastUpdated.format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralVar40
                )
            }

            if (drrData == null && !loading) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.Analytics, null,
                        tint = NeutralVar40, modifier = Modifier.size(56.dp))
                    Text("Sin datos disponibles",
                        style = MaterialTheme.typography.bodyMedium, color = NeutralVar50)
                    ProButton("Leer Datos", onClick = { vm.fetchDRRData() },
                        icon = Icons.Rounded.Refresh, modifier = Modifier.fillMaxWidth(0.6f))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SystemStatusChip(
    label    : String,
    active   : Boolean,
    icon     : androidx.compose.ui.graphics.vector.ImageVector,
    modifier : Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = if (active) AppColors.tagGreen else NeutralVar20,
        border   = BorderStroke(1.dp, if (active) Emerald40.copy(0.4f) else AppColors.divider),
        modifier = modifier
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null,
                tint = if (active) AppColors.success else NeutralVar50,
                modifier = Modifier.size(16.dp))
            Column {
                Text(label,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralVar60)
                Text(if (active) "ON" else "OFF",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) AppColors.success else NeutralVar50,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

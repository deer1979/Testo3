package com.containerpro.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.model.TemperatureData
import com.containerpro.model.ElectricalData
import com.containerpro.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DRRScreen(navController: NavController) {
    // Simulated live data — replace with ViewModel StateFlow in production
    var temps     by remember { mutableStateOf(TemperatureData()) }
    var elec      by remember { mutableStateOf(ElectricalData(voltageL1 = 220.8f, voltageL2 = 219.4f, voltageL3 = 221.2f, currentComp = 14.3f, powerKw = 4.2f, frequencyHz = 60.0f, powerFactor = 0.87f)) }
    var isLoading by remember { mutableStateOf(false) }

    // Simulate a refresh
    LaunchedEffect(Unit) {
        isLoading = true
        delay(800)
        isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.drr_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isLoading = true
                        /* trigger re-read */
                    }) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Rounded.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Temperature card ──────────────────────────
            DRRCard(title = stringResource(R.string.temperatures)) {
                val tempRows = listOf(
                    Triple(stringResource(R.string.supply_air), temps.supplyAir, "°C"),
                    Triple(stringResource(R.string.return_air), temps.returnAir, "°C"),
                    Triple(stringResource(R.string.setpoint),   temps.setpoint,  "°C"),
                    Triple(stringResource(R.string.evap_coil),  temps.evapCoil,  "°C"),
                    Triple(stringResource(R.string.cond_coil),  temps.condCoil,  "°C"),
                    Triple(stringResource(R.string.ambient),    temps.ambient,   "°C"),
                    Triple(stringResource(R.string.discharge),  temps.discharge, "°C"),
                    Triple(stringResource(R.string.suction),    temps.suction,   "°C"),
                )
                tempRows.forEachIndexed { i, (label, value, unit) ->
                    DRRRow(label = label, value = "%.1f".format(value), unit = unit)
                    if (i < tempRows.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }

            // ── Electrical card ───────────────────────────
            DRRCard(title = stringResource(R.string.electrical_data)) {
                // Voltage phase bars
                listOf(
                    Triple("L1", elec.voltageL1, 230f),
                    Triple("L2", elec.voltageL2, 230f),
                    Triple("L3", elec.voltageL3, 230f),
                ).forEach { (phase, voltage, max) ->
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(phase, style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(24.dp))
                        // ✅ Correct API: progress = { lambda } (not Float directly)
                        LinearProgressIndicator(
                            progress = { (voltage / max).coerceIn(0f, 1f) },
                            modifier = Modifier.weight(1f).height(6.dp),
                            color    = MaterialTheme.colorScheme.primary,
                        )
                        Text("%.0f V".format(voltage),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(60.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))

                val elecRows = listOf(
                    Triple(stringResource(R.string.compressor), "%.1f A".format(elec.currentComp), ""),
                    Triple(stringResource(R.string.power),      "%.1f kW".format(elec.powerKw),    ""),
                    Triple(stringResource(R.string.frequency),  "%.1f Hz".format(elec.frequencyHz),""),
                    Triple("Power Factor", "%.2f".format(elec.powerFactor), ""),
                )
                elecRows.forEachIndexed { i, (label, value, _) ->
                    DRRRow(label = label, value = value, unit = "")
                    if (i < elecRows.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }

            // ── Alarms card ───────────────────────────────
            DRRCard(title = stringResource(R.string.active_alarms)) {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                    Text(stringResource(R.string.no_alarms), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DRRCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}

@Composable
private fun DRRRow(label: String, value: String, unit: String) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "$value $unit".trim(),
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
        )
    }
}

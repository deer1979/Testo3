package com.containerpro.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.navigation.Screen
import com.containerpro.ui.components.ProButton
import com.containerpro.ui.theme.*

// ── Model Select ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectScreen(navController: NavController) {
    val ml3Models = listOf("ML3 PLUS","ML3 SMART","ML3 ULTRA")
    val ml5Models = listOf("ML5 NaturaLine","ML5 OptimalINE","ML5 PrimeLine","ML5 SureTemp")
    var selected  by remember { mutableStateOf("ML5 OptimalINE") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.model_select_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { p ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(p).padding(16.dp)) {
            Text(stringResource(R.string.model_select_sub), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 20.dp))

            listOf(stringResource(R.string.ml3_section) to ml3Models, stringResource(R.string.ml5_section) to ml5Models).forEach { (section, models) ->
                Text(section, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp))
                Surface(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Column {
                        models.forEach { model ->
                            val isSelected = selected == model
                            Row(
                                Modifier.fillMaxWidth().clickable { selected = model }.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(model, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                                if (isSelected) Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            if (model != models.last()) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            ProButton(stringResource(R.string.confirm), onClick = { navController.popBackStack() },
                icon = Icons.Rounded.Check, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ── Service Check ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCheckScreen(navController: NavController) {
    val categories = mapOf(
        "Visual" to listOf("Daños externos","Puertas / sellos","Drenajes libres","Condición eléctrica"),
        "Mecánico" to listOf("Compresor","Ventiladores evap.","Ventiladores cond.","Correas / tensores"),
        "Refrigeración" to listOf("Nivel de refrigerante","Presión de succión","Presión de descarga","Válvulas"),
        "Eléctrico" to listOf("Tensión entrada","Balance de fases","Amperaje compresor","Contactores / relés"),
    )
    val checked = remember { mutableStateMapOf<String, Boolean>() }
    val done    = checked.values.count { it }
    val total   = categories.values.sumOf { it.size }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checklist_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.navigationBarsPadding()) {
                ProButton(
                    stringResource(R.string.complete_service),
                    onClick  = { navController.popBackStack() },
                    icon     = Icons.Rounded.CheckCircle,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }
        },
    ) { p ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(p).padding(16.dp)) {
            LinearProgressIndicator(
                progress = { done.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color    = MaterialTheme.colorScheme.primary,
            )
            Text("$done / $total ${stringResource(R.string.items_completed)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp))

            categories.forEach { (cat, items) ->
                Text(cat, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                Surface(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Column {
                        items.forEach { item ->
                            Row(
                                Modifier.fillMaxWidth().clickable { checked[item] = !(checked[item] ?: false) }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(item, style = MaterialTheme.typography.bodyMedium,
                                    color = if (checked[item] == true) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurface)
                                Checkbox(
                                    checked  = checked[item] ?: false,
                                    onCheckedChange = { checked[item] = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                                )
                            }
                            if (item != items.last()) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

// ── Photos ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.photos_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Rounded.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.photos), fontWeight = FontWeight.Bold)
            }
        },
    ) { p ->
        Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.PhotoCamera, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                Text(stringResource(R.string.no_photos), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Reports ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { p ->
        Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                Text(stringResource(R.string.no_reports), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.no_reports_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

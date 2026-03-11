package com.containerpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.data.PreferencesManager
import com.containerpro.model.ContainerSession
import com.containerpro.navigation.Screen
import com.containerpro.ui.components.*
import com.containerpro.ui.theme.*
import com.containerpro.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController    : NavController,
    prefs            : PreferencesManager,
    onThemeChange    : (AppTheme) -> Unit,
    onLanguageChange : (String) -> Unit,
    vm               : MainViewModel = viewModel(),
) {
    val session       by vm.currentSession.collectAsState()
    val wifiConnected by vm.wifiConnected.collectAsState()
    val currentTheme  by prefs.theme.collectAsState()
    var showSettings  by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Emerald40, Emerald60))),
                            contentAlignment = Alignment.Center,
                        ) { Text("📦", fontSize = 18.sp) }
                        Text("ContainerPro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // Theme toggle
                    IconButton(onClick = {
                        onThemeChange(if (currentTheme == AppTheme.LIGHT) AppTheme.DARK else AppTheme.LIGHT)
                    }) {
                        Icon(
                            imageVector = if (currentTheme == AppTheme.LIGHT) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // Language
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Rounded.Translate, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Avatar
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { navController.navigate(Screen.Settings.route) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = (session?.technicianId?.take(2) ?: "TE").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = true,  onClick = {},
                    icon  = { Icon(Icons.Rounded.Home, null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                )
                NavigationBarItem(
                    selected = false, onClick = { navController.navigate(Screen.Scan.route) },
                    icon  = { Icon(Icons.Rounded.QrCodeScanner, null) },
                    label = { Text(stringResource(R.string.nav_scan)) },
                )
                NavigationBarItem(
                    selected = false, onClick = { navController.navigate(Screen.DRR.route) },
                    icon  = { Icon(Icons.Rounded.Analytics, null) },
                    label = { Text(stringResource(R.string.nav_drr)) },
                )
                NavigationBarItem(
                    selected = false, onClick = { navController.navigate(Screen.Settings.route) },
                    icon  = { Icon(Icons.Rounded.Settings, null) },
                    label = { Text(stringResource(R.string.nav_control)) },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
        ) {
            // ── Hero card ─────────────────────────────────
            HeroCard(session)

            Spacer(Modifier.height(20.dp))

            // ── Main 2×2 grid ─────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionLabel(stringResource(R.string.main_tools))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard(
                        title   = stringResource(R.string.scan_container),
                        sub     = stringResource(R.string.ocr_iso),
                        icon    = Icons.Rounded.QrCodeScanner,
                        palette = emeraldPalette(),
                        onClick = { navController.navigate(Screen.Scan.route) },
                        modifier= Modifier.weight(1f),
                    )
                    FeatureCard(
                        title   = "WiFi Direct",
                        sub     = if (wifiConnected) stringResource(R.string.ml5_connected)
                                  else stringResource(R.string.no_wifi),
                        icon    = Icons.Rounded.Wifi,
                        palette = amberPalette(),
                        onClick = { navController.navigate(Screen.WifiConnect.route) },
                        modifier= Modifier.weight(1f),
                        badge   = { StatusPill(if (wifiConnected) "LIVE" else "OFF", wifiConnected) },
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard(
                        title   = stringResource(R.string.drr_diagnostics),
                        sub     = stringResource(R.string.temps_alarms),
                        icon    = Icons.Rounded.Analytics,
                        palette = tealPalette(),
                        onClick = { navController.navigate(Screen.DRR.route) },
                        modifier= Modifier.weight(1f),
                    )
                    FeatureCard(
                        title   = stringResource(R.string.relay_control),
                        sub     = stringResource(R.string.service_mode),
                        icon    = Icons.Rounded.Tune,
                        palette = violetPalette(),
                        onClick = {
                            val ip = vm.groupOwnerIp
                            if (wifiConnected && ip != null)
                                navController.navigate(Screen.ServiceControl.createRoute(ip))
                            else
                                navController.navigate(Screen.WifiConnect.route)
                        },
                        modifier= Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Wide service cards ────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionLabel(stringResource(R.string.service))
                WideCard(
                    title   = stringResource(R.string.service_checklist),
                    sub     = "8 / 16 ${stringResource(R.string.items_completed)}",
                    icon    = Icons.Rounded.FactCheck,
                    iconBg  = Emerald60.copy(alpha = 0.12f),
                    onClick = { navController.navigate(Screen.ServiceCheck.route) },
                    modifier= Modifier.padding(bottom = 8.dp),
                )
                WideCard(
                    title   = stringResource(R.string.photos),
                    sub     = "3 ${stringResource(R.string.photos_captured)}",
                    icon    = Icons.Rounded.CameraAlt,
                    iconBg  = Amber70.copy(alpha = 0.12f),
                    onClick = { navController.navigate(Screen.Photos.route) },
                    modifier= Modifier.padding(bottom = 8.dp),
                )
                WideCard(
                    title   = stringResource(R.string.reports),
                    sub     = "5 ${stringResource(R.string.reports_saved)}",
                    icon    = Icons.Rounded.Description,
                    iconBg  = Coral80.copy(alpha = 0.10f),
                    onClick = { navController.navigate(Screen.Reports.route) },
                    modifier= Modifier.padding(bottom = 8.dp),
                )
                WideCard(
                    title   = stringResource(R.string.settings),
                    sub     = stringResource(R.string.settings_sub),
                    icon    = Icons.Rounded.Settings,
                    iconBg  = Violet80.copy(alpha = 0.10f),
                    onClick = { navController.navigate(Screen.Settings.route) },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // Quick settings sheet
    if (showSettings) {
        QuickSettingsSheet(
            prefs            = prefs,
            onThemeChange    = onThemeChange,
            onLanguageChange = onLanguageChange,
            onDismiss        = { showSettings = false },
        )
    }
}

// ── Hero session card ─────────────────────────────────────
@Composable
private fun HeroCard(session: ContainerSession?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text       = if (session != null) "● SESIÓN ACTIVA" else "○ SIN SESIÓN",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
            Text(
                text       = session?.containerId ?: "— — — — — — — —",
                style      = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(top = 4.dp, bottom = 2.dp),
            )
            Text(
                text     = session?.modelName ?: "Sin modelo seleccionado",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 18.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroStat("−18.3°", "Supply Air", Modifier.weight(1f))
                HeroStat("220 V",  "Red L1",     Modifier.weight(1f))
                HeroStat("0 ⚠",   "Alarmas",    Modifier.weight(1f))
            }
        }
    }
}

package com.containerpro.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.data.PreferencesManager
import com.containerpro.navigation.Screen
import com.containerpro.ui.components.*
import com.containerpro.ui.theme.*
import com.containerpro.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController     : NavController,
    prefs             : PreferencesManager,
    onThemeChange     : (AppTheme) -> Unit,
    onLanguageChange  : (String) -> Unit,
    vm                : MainViewModel = viewModel(),
) {
    val session       = vm.currentSession.collectAsState().value
    val wifiConnected = vm.wifiConnected.collectAsState().value
    var showSettings  by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar         = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Emerald40, Emerald60))),
                            contentAlignment = Alignment.Center,
                        ) { Text("📦", fontSize = 18.sp) }
                        Text("ContainerPro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // Theme quick toggle
                    val currentTheme = prefs.theme.collectAsState().value
                    IconButton(onClick = {
                        onThemeChange(if (currentTheme == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK)
                    }) {
                        Icon(
                            imageVector = if (currentTheme == AppTheme.LIGHT) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // Settings / language
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Rounded.Translate, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Avatar
                    Box(
                        modifier = Modifier.padding(end = 8.dp).size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { navController.navigate(Screen.Settings.route) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (session?.technicianId?.take(2) ?: "TE").uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = { HomeBottomBar(navController) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
        ) {
            // ── Hero session card ──────────────────────────
            HeroSessionCard(session, vm)

            Spacer(Modifier.height(20.dp))

            // ── Main tools grid ────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionTitle(stringResource(R.string.main_tools))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard(
                        title   = stringResource(R.string.scan_container),
                        sub     = stringResource(R.string.ocr_iso),
                        icon    = Icons.Rounded.QrCodeScanner,
                        colors  = emeraldCard(),
                        onClick = { navController.navigate(Screen.Scan.route) },
                        modifier= Modifier.weight(1f),
                    )
                    FeatureCard(
                        title   = "WiFi Direct",
                        sub     = if (wifiConnected) stringResource(R.string.ml5_connected) else stringResource(R.string.no_wifi),
                        icon    = Icons.Rounded.Wifi,
                        colors  = amberCard(),
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
                        colors  = tealCard(),
                        onClick = { navController.navigate(Screen.DRR.route) },
                        modifier= Modifier.weight(1f),
                    )
                    FeatureCard(
                        title   = stringResource(R.string.relay_control),
                        sub     = stringResource(R.string.service_mode),
                        icon    = Icons.Rounded.Tune,
                        colors  = violetCard(),
                        onClick = {
                            if (wifiConnected && vm.groupOwnerIp != null)
                                navController.navigate(Screen.ServiceControl.createRoute(vm.groupOwnerIp!!))
                        },
                        modifier= Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Service wide cards ─────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionTitle(stringResource(R.string.service))
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
                    onClick = { showSettings = true },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Quick settings bottom sheet ────────────────────────
    if (showSettings) {
        QuickSettingsSheet(
            prefs            = prefs,
            onThemeChange    = onThemeChange,
            onLanguageChange = onLanguageChange,
            onDismiss        = { showSettings = false },
        )
    }
}

// ── Hero card ─────────────────────────────────────────────
@Composable
private fun HeroSessionCard(
    session: com.containerpro.model.ContainerSession?,
    vm     : MainViewModel,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
        shape    = RoundedCornerShape(28.dp),
        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
    ) {
        Box {
            // Decorative circle
            Box(
                Modifier.size(160.dp).offset(x = 220.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            )
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text  = if (session != null) stringResource(R.string.active_session)
                            else stringResource(R.string.no_active_session),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                )
                Text(
                    text  = session?.containerId ?: "— — — — — — — —",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                )
                Text(
                    text  = session?.modelName ?: stringResource(R.string.select_model),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 18.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroStat("−18.3°", stringResource(R.string.supply_air), Modifier.weight(1f))
                    HeroStat("220 V", "Red L1", Modifier.weight(1f))
                    HeroStat("0 ⚠", stringResource(R.string.alarms), Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────
@Composable
private fun HomeBottomBar(navController: NavController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = true,
            onClick  = { },
            icon     = { Icon(Icons.Rounded.Home, null) },
            label    = { Text(stringResource(R.string.nav_home)) },
        )
        NavigationBarItem(
            selected = false,
            onClick  = { navController.navigate(Screen.Scan.route) },
            icon     = { Icon(Icons.Rounded.QrCodeScanner, null) },
            label    = { Text(stringResource(R.string.nav_scan)) },
        )
        NavigationBarItem(
            selected = false,
            onClick  = { navController.navigate(Screen.DRR.route) },
            icon     = { Icon(Icons.Rounded.Analytics, null) },
            label    = { Text(stringResource(R.string.nav_drr)) },
        )
        NavigationBarItem(
            selected = false,
            onClick  = { navController.navigate(Screen.Settings.route) },
            icon     = { Icon(Icons.Rounded.Settings, null) },
            label    = { Text(stringResource(R.string.nav_control)) },
        )
    }
}

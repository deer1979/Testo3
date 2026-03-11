package com.containerpro.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.data.PreferencesManager
import com.containerpro.navigation.Screen
import com.containerpro.ui.components.ThemeChipRow
import com.containerpro.ui.theme.*

data class LangOption(val code: String, val flag: String, val label: String, val name: String)

private val LANGUAGES = listOf(
    LangOption("es", "🇪🇸", "ES", "Español"),
    LangOption("en", "🇺🇸", "EN", "English"),
    LangOption("pt", "🇧🇷", "PT", "Português"),
    LangOption("fr", "🇫🇷", "FR", "Français"),
    LangOption("zh", "🇨🇳", "ZH", "中文"),
    LangOption("nl", "🇳🇱", "NL", "Nederlands"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController    : NavController,
    preferencesManager: PreferencesManager,
    onThemeChange    : (AppTheme) -> Unit,
    onLanguageChange : (String) -> Unit,
) {
    val currentTheme by preferencesManager.theme.collectAsState()
    val currentLang  by preferencesManager.language.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // ── Theme section ──────────────────────────────
            SettingsSection(title = stringResource(R.string.theme), icon = "🎨") {
                ThemeChipRow(
                    current  = currentTheme,
                    onChange = onThemeChange,
                )
            }

            // ── Language section ───────────────────────────
            SettingsSection(title = stringResource(R.string.language), icon = "🌐") {
                val cols = 3
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LANGUAGES.chunked(cols).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { lang ->
                                LangButton(
                                    lang     = lang,
                                    selected = currentLang == lang.code,
                                    onClick  = { onLanguageChange(lang.code) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // Fill empty slots
                            repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // ── App info section ───────────────────────────
            SettingsSection(title = stringResource(R.string.session), icon = "🔧") {
                InfoRow(Icons.Rounded.Badge,     stringResource(R.string.technician),   preferencesManager.loadTechnicianId().ifEmpty { "TE-001" })
                InfoRow(Icons.Rounded.Info,      stringResource(R.string.version),      "1.0.0")
                InfoRow(Icons.Rounded.Storage,   stringResource(R.string.data_storage), stringResource(R.string.data_local))
                InfoRow(Icons.Rounded.Wifi,      stringResource(R.string.wifi_protocol),stringResource(R.string.wifi_p2p))
                InfoRow(Icons.Rounded.DocumentScanner, stringResource(R.string.ocr_engine), stringResource(R.string.ocr_mlkit))
            }

            // ── Logout ─────────────────────────────────────
            OutlinedButton(
                onClick  = {
                    preferencesManager.setLoggedIn(false)
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = ErrorCoral),
                border   = BorderStroke(1.dp, ErrorCoral.copy(alpha = 0.4f)),
            ) {
                Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title  : String,
    icon   : String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(icon, fontSize = 20.sp)
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            content()
        }
    }
}

@Composable
private fun LangButton(lang: LangOption, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick  = onClick,
        modifier = modifier
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(16.dp),
            ),
        shape    = RoundedCornerShape(16.dp),
        color    = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                   else          MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier           = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(lang.flag, fontSize = 22.sp)
            Text(lang.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(lang.name, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

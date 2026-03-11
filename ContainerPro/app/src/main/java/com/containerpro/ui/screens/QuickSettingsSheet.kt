package com.containerpro.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.containerpro.R
import com.containerpro.data.PreferencesManager
import com.containerpro.ui.components.ThemeChipRow
import com.containerpro.ui.theme.AppTheme

/**
 * Lightweight bottom sheet accessible from the Home toolbar —
 * quick access to theme + language without going to the full Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsSheet(
    prefs           : PreferencesManager,
    onThemeChange   : (AppTheme) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDismiss       : () -> Unit,
) {
    val currentTheme by prefs.theme.collectAsState()
    val currentLang  by prefs.language.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        containerColor    = MaterialTheme.colorScheme.surface,
        shape             = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle        = {
            Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(40.dp, 4.dp), shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.outline) {}
            }
        },
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Theme
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎨  ${stringResource(R.string.theme)}",
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ThemeChipRow(current = currentTheme, onChange = onThemeChange)
            }

            // Language
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌐  ${stringResource(R.string.language)}",
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("es","🇪🇸","ES"),
                        Triple("en","🇺🇸","EN"),
                        Triple("pt","🇧🇷","PT"),
                        Triple("fr","🇫🇷","FR"),
                        Triple("zh","🇨🇳","ZH"),
                        Triple("nl","🇳🇱","NL"),
                    ).forEach { (code, flag, label) ->
                        val selected = currentLang == code
                        Surface(
                            onClick  = { onLanguageChange(code) },
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp,
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(14.dp)),
                            shape  = RoundedCornerShape(14.dp),
                            color  = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(0.4f)
                                     else          MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(flag, fontSize = 20.sp)
                                Text(label, style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

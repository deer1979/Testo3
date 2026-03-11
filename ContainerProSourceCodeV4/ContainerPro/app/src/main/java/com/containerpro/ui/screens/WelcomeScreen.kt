package com.containerpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.data.PreferencesManager
import com.containerpro.navigation.Screen
import com.containerpro.ui.theme.Emerald40
import com.containerpro.ui.theme.Emerald60

@Composable
fun WelcomeScreen(navController: NavController, prefs: PreferencesManager) {
    var techId by remember { mutableStateOf("") }
    var pin    by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // Radial background glow
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background,
                        )
                    )
                )
        )

        Column(
            modifier           = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Emerald40, Emerald60))),
                contentAlignment = Alignment.Center,
            ) {
                Text("📦", fontSize = 38.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ContainerPro",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.field_service_tool),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value         = techId,
                onValueChange = { techId = it },
                label         = { Text(stringResource(R.string.technician_id)) },
                leadingIcon   = { Icon(Icons.Rounded.Badge, null) },
                singleLine    = true,
                shape         = RoundedCornerShape(16.dp),
                modifier      = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value                = pin,
                onValueChange        = { pin = it },
                label                = { Text(stringResource(R.string.pin)) },
                leadingIcon          = { Icon(Icons.Rounded.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine           = true,
                shape                = RoundedCornerShape(16.dp),
                modifier             = Modifier.fillMaxWidth(),
            )

            Button(
                onClick  = {
                    prefs.saveTechnicianId(techId.ifEmpty { "TE-001" })
                    prefs.setLoggedIn(true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.login), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp))
            }

            Text(
                stringResource(R.string.local_data_note),
                style  = MaterialTheme.typography.labelSmall,
                color  = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

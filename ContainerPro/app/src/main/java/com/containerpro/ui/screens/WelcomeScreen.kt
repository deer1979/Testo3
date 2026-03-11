package com.containerpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.data.PreferencesManager
import com.containerpro.navigation.Screen
import com.containerpro.ui.components.ProButton
import com.containerpro.ui.theme.*

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
        // Background gradient
        Box(
            Modifier.fillMaxSize().background(
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
            ) { Text("📦", fontSize = 38.sp) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ContainerPro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.field_service_tool),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
            OutlinedTextField(
                value         = pin,
                onValueChange = { pin = it },
                label         = { Text(stringResource(R.string.pin)) },
                leadingIcon   = { Icon(Icons.Rounded.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine    = true,
                shape         = RoundedCornerShape(16.dp),
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
            ProButton(
                text     = stringResource(R.string.login),
                onClick  = {
                    prefs.saveTechnicianId(techId.ifEmpty { "TE-001" })
                    prefs.setLoggedIn(true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                icon     = Icons.Rounded.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.local_data_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

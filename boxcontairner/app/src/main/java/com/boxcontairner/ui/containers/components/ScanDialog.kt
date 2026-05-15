package com.boxcontairner.ui.containers.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.boxcontairner.domain.model.Container
import com.boxcontairner.ui.scan.CameraScanner
import com.boxcontairner.util.ScanMode
import com.boxcontairner.util.ScanResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDialog(onDismiss: () -> Unit, onScanned: (Container) -> Unit) {
    var code         by remember { mutableStateOf("") }
    var serial       by remember { mutableStateOf("") }
    var model        by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var year         by remember { mutableStateOf("") }
    var activeScanMode by remember { mutableStateOf<ScanMode?>(null) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) activeScanMode = ScanMode.CONTAINER_ID
    }

    if (activeScanMode != null) {
        BasicAlertDialog(
            onDismissRequest = { activeScanMode = null },
            modifier = Modifier.fillMaxWidth().height(550.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), shape = MaterialTheme.shapes.extraLarge) {
                Box {
                    CameraScanner(mode = activeScanMode!!, onResult = { result ->
                        when (result) {
                            is ScanResult.ContainerId -> { code = result.code; activeScanMode = null }
                            is ScanResult.ReeferInfo  -> {
                                serial = result.data.serial
                                model = result.data.model
                                manufacturer = result.data.manufacturer
                                year = result.data.year
                                activeScanMode = null
                            }
                        }
                    })
                    IconButton(
                        onClick = { activeScanMode = null },
                        modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Registrar Unidad", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().trim() },
                        label = { Text("ID Contenedor (ISO)") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_GRANTED
                                ) activeScanMode = ScanMode.CONTAINER_ID
                                else permissionLauncher.launch(Manifest.permission.CAMERA)
                            }) { Icon(Icons.Default.PhotoCamera, null) }
                        }
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (serial.isBlank()) Color(0xFFF5F5F5) else Color(0xFFE8F5E9)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { activeScanMode = ScanMode.REEFER_PLATE }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (serial.isBlank()) Icons.Default.SettingsSystemDaydream else Icons.Default.CheckCircle,
                                null,
                                tint = if (serial.isBlank()) Color.Gray else Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    if (serial.isBlank()) "Escanear Placa Reefer" else "Placa Capturada ✓",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (serial.isNotBlank()) {
                                    Text(
                                        "$manufacturer $model",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                    if (code.isNotBlank()) {
                        Text(
                            "Podrás editar y validar todos los datos en la ficha del equipo.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onScanned(
                            Container(
                                code = code.uppercase().trim(),
                                type = "Reefer",
                                reeferSerialNumber = serial,
                                reeferModel = model,
                                reeferManufacturer = manufacturer,
                                reeferYear = year
                            )
                        )
                    },
                    enabled = code.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (serial.isBlank()) "CONTINUAR SIN PLACA" else "ABRIR FICHA")
                }
            }
        )
    }
}

package com.boxcontairner.ui.containers.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boxcontairner.R
import com.boxcontairner.domain.model.Container
import com.boxcontairner.domain.model.ContainerStatus
import com.boxcontairner.domain.model.WorkInstruction
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ContainerItem(container: Container, onClick: () -> Unit) {
    var showNote by remember { mutableStateOf(false) }
    val status = container.statusEnum

    val statusColor = when (status) {
        ContainerStatus.OK   -> Color(0xFF2E7D32)
        ContainerStatus.EST  -> Color(0xFFFBC02D)
        ContainerStatus.DMG  -> Color(0xFFD32F2F)
        ContainerStatus.INSP -> Color(0xFF757575)
    }

    val brandPainter = when (container.reeferManufacturer.uppercase()) {
        "CARRIER"     -> painterResource(id = R.drawable.ic_carrier)
        "STAR COOL"   -> painterResource(id = R.drawable.ic_starcool3)
        "DAIKIN"      -> painterResource(id = R.drawable.ic_daikin)
        "THERMO KING" -> painterResource(id = R.drawable.ic_thermokig)
        else          -> null
    }
    val isStarCool = container.reeferManufacturer.uppercase() == "STAR COOL"
    val variant = container.reeferVariant.ifBlank {
        if (container.reeferModel.uppercase().endsWith("-CA")) "CA" else "STD"
    }

    if (showNote) {
        AlertDialog(
            onDismissRequest = { showNote = false },
            title = { Text("Observaciones", fontWeight = FontWeight.Bold) },
            text = { Text(container.observations) },
            confirmButton = {
                TextButton(onClick = { showNote = false }) { Text("CERRAR") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {

            // Fila 1: código + icono nota (si existe) + chip de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        container.code,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontSize = 19.sp
                    )
                    if (container.observations.isNotBlank()) {
                        IconButton(
                            onClick = { showNote = true },
                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "Ver Nota",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = statusColor,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.width(75.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            status.displayLabel,
                            color = if (status == ContainerStatus.EST) Color.Black else Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black
                        )
                        Icon(
                            Icons.Default.ChevronRight, null,
                            tint = if (status == ContainerStatus.EST) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Fila 2: chips Instruccion + CA/STD
            val instr = container.instruccionEnum
            val showChips = instr != WorkInstruction.NONE || isStarCool
            if (showChips) {
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (instr != WorkInstruction.NONE) {
                        val instrColor = if (instr == WorkInstruction.FULL_PTI)
                            Color(0xFF1565C0) else Color(0xFFFF5722)
                        Surface(color = instrColor, shape = MaterialTheme.shapes.extraSmall) {
                            Text(
                                instr.displayLabel,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isStarCool) {
                        val varColor = if (variant == "CA") Color(0xFF00BCD4) else Color(0xFF1565C0)
                        Surface(color = varColor, shape = MaterialTheme.shapes.extraSmall) {
                            Text(
                                variant,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Fila 3: info del reefer
            if (container.reeferSerialNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFFF8F9FA), shape = MaterialTheme.shapes.extraSmall)
                        .border(0.5.dp, Color(0xFFEEEEEE), shape = MaterialTheme.shapes.extraSmall)
                        .padding(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (brandPainter != null) {
                            Icon(
                                painter = brandPainter,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.Unspecified
                            )
                        } else {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.DarkGray
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${container.reeferManufacturer} ${container.reeferModel}".trim(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            fontSize = 11.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "S/N: ${container.reeferSerialNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF424242),
                            fontSize = 10.sp
                        )
                        if (container.reeferYear.isNotBlank()) {
                            Text(
                                "AÑO: ${container.reeferYear}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF424242),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Fila 4: operador + fecha
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Op: ${container.updatedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
                Text(
                    SimpleDateFormat("dd/MM HH:mm", LocalLocale.current.platformLocale)
                        .format(Date(container.lastUpdate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

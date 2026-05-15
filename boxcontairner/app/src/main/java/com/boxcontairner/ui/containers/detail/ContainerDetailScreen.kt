package com.boxcontairner.ui.containers.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxcontairner.domain.model.ContainerStatus
import com.boxcontairner.domain.model.StatusHistory
import com.boxcontairner.domain.model.WorkInstruction
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDetailScreen(
    onBack: () -> Unit,
    viewModel: ContainerDetailViewModel = hiltViewModel()
) {
    val container by viewModel.container.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val role by viewModel.currentRole.collectAsStateWithLifecycle()

    if (container == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val c = container!!
    var editMode by remember { mutableStateOf(viewModel.startInEditMode) }
    var edited by remember(c.id) { mutableStateOf(c) }
    var showValidationMsg by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val validInstructions = listOf(WorkInstruction.FULL_PTI, WorkInstruction.VISUAL_CHECK)
    val missingFields = buildList {
        if (edited.code.isBlank()) add("ID Unidad")
        if (edited.reeferManufacturer.isBlank()) add("Fabricante")
        if (edited.reeferModel.isBlank()) add("Modelo")
        if (edited.reeferSerialNumber.isBlank()) add("Serial")
        if (edited.reeferYear.isBlank()) add("Año")
        if (!validInstructions.contains(edited.instruccionEnum)) add("Instrucción")
        if (edited.status.isBlank()) add("Estado")
    }
    val isValid = missingFields.isEmpty()

    BackHandler(enabled = editMode) {
        if (isValid) editMode = false else showValidationMsg = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(c.code, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editMode) {
                            if (isValid) editMode = false else showValidationMsg = true
                        } else onBack()
                    }) {
                        val tint = if (editMode && !isValid) Color.Gray else LocalContentColor.current
                        Icon(
                            if (editMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            null, tint = tint
                        )
                    }
                },
                actions = {
                    if (editMode) {
                        IconButton(
                            onClick = {
                                if (isValid) {
                                    viewModel.saveDetail(edited)
                                    editMode = false
                                    showValidationMsg = false
                                } else showValidationMsg = true
                            }
                        ) {
                            Icon(
                                Icons.Default.Check, "Guardar",
                                tint = if (isValid) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    } else if (role.canManageContainers) {
                        // Solo ADMIN/SUPER_ADMIN pueden editar
                        IconButton(onClick = { editMode = true }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showValidationMsg && !isValid) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Completar para continuar",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "Faltan: ${missingFields.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // SECCIÓN ESTADOS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Estado de Inspección",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ContainerStatus.entries.forEach { s ->
                            val isSelected = edited.statusEnum == s
                            val color = when (s) {
                                ContainerStatus.OK -> Color(0xFF2E7D32)
                                ContainerStatus.EST -> Color(0xFFFBC02D)
                                ContainerStatus.DMG -> Color(0xFFD32F2F)
                                ContainerStatus.INSP -> Color(0xFF757575)
                            }
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = isSelected,
                                onClick = {
                                    if (editMode) {
                                        edited = edited.copy(status = s.name)
                                        showValidationMsg = false
                                    }
                                },
                                label = {
                                    Text(
                                        s.displayLabel,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color,
                                    selectedLabelColor = if (s == ContainerStatus.EST) Color.Black else Color.White,
                                    labelColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }

            // SECCIÓN INSTRUCCIONES
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Instrucción de Trabajo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        validInstructions.forEach { opt ->
                            val selected = edited.instruccionEnum == opt
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (editMode) {
                                        edited = edited.copy(instruccion = opt.displayLabel)
                                        showValidationMsg = false
                                    }
                                },
                                label = { Text(opt.displayLabel, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    labelColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }

            // DATOS TÉCNICOS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditableRow("Fabricante", if (editMode) edited.reeferManufacturer else c.reeferManufacturer, editMode, uppercase = true) {
                            edited = edited.copy(reeferManufacturer = it); showValidationMsg = false
                        }
                        EditableRow("Modelo", if (editMode) edited.reeferModel else c.reeferModel, editMode, uppercase = true) {
                            edited = edited.copy(reeferModel = it); showValidationMsg = false
                        }
                        EditableRow("Serial", if (editMode) edited.reeferSerialNumber else c.reeferSerialNumber, editMode, uppercase = true) {
                            edited = edited.copy(reeferSerialNumber = it); showValidationMsg = false
                        }
                        EditableRow("Año", if (editMode) edited.reeferYear else c.reeferYear, editMode, keyboardType = KeyboardType.Number) {
                            edited = edited.copy(reeferYear = it); showValidationMsg = false
                        }

                        if (editMode) {
                            OutlinedTextField(
                                value = edited.observations,
                                onValueChange = { edited = edited.copy(observations = it) },
                                label = { Text("Observaciones (Opcional)") },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                minLines = 2
                            )
                        } else if (c.observations.isNotBlank()) {
                            Column(modifier = Modifier.padding(top = 4.dp)) {
                                Text("Observaciones", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(c.observations, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // BOTÓN DE ELIMINAR — solo ADMIN/SUPER_ADMIN
            if (!editMode && role.canManageContainers) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar Registro", fontSize = 13.sp)
                    }
                }
            }

            // HISTORIAL
            item {
                Text(
                    "Trazabilidad de Unidad",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (history.isEmpty()) {
                item {
                    Text(
                        "Sin eventos registrados",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                items(history) { TimelineEntry(it) }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar Unidad?") },
            text = { Text("Esta acción es irreversible y quitará la unidad del inventario activo.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteOrArchive { onBack() }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCELAR") }
            }
        )
    }
}

@Composable
private fun EditableRow(
    label: String,
    value: String,
    editMode: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    uppercase: Boolean = false,
    onChange: (String) -> Unit
) {
    if (editMode) {
        OutlinedTextField(
            value = value,
            onValueChange = { v -> onChange(if (uppercase) v.uppercase() else v) },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = if (uppercase) KeyboardCapitalization.Characters else KeyboardCapitalization.None
            )
        )
    } else {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.width(90.dp)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TimelineEntry(entry: StatusHistory) {
    val platformLocale = LocalLocale.current.platformLocale
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
            Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            Box(Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "${entry.action}: ${entry.previousStatus} → ${entry.newStatus}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Por: ${entry.changedBy} · ${SimpleDateFormat("dd/MM HH:mm", platformLocale).format(Date(entry.timestamp))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

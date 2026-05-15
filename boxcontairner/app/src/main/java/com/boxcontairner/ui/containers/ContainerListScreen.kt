package com.boxcontairner.ui.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxcontairner.ui.containers.components.ContainerItem
import com.boxcontairner.ui.containers.components.ScanDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerListScreen(
    onContainerClick: (String) -> Unit,
    onContainerScanned: (String) -> Unit,
    viewModel: ContainerListViewModel = hiltViewModel()
) {
    val containers by viewModel.containers.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateToDetail.collect { id -> onContainerScanned(id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Logística de Campo", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(containers, key = { it.id }) { container ->
                ContainerItem(container, onClick = { onContainerClick(container.id) })
            }
        }
    }

    if (showDialog) {
        ScanDialog(
            onDismiss = { showDialog = false },
            onScanned = { container ->
                showDialog = false
                viewModel.scanAndNavigate(container)
            }
        )
    }
}

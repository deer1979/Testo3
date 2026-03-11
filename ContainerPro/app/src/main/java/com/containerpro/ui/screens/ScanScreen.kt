package com.containerpro.ui.screens

import android.Manifest
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.containerpro.navigation.Screen
import com.containerpro.ui.components.*
import com.containerpro.ui.theme.*
import com.containerpro.viewmodel.MainViewModel
import java.util.concurrent.Executors

// ISO 6346 container number pattern: 4 letters + 6 digits + 1 check digit
private val CONTAINER_REGEX = Regex("[A-Z]{4}\\d{7}")

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(navController: NavController, vm: MainViewModel = viewModel()) {

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var scannedText  by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }
    var manualInput  by remember { mutableStateOf(false) }
    var manualText   by remember { mutableStateOf("") }

    // Scanning line animation
    val scanAnim = rememberInfiniteTransition(label = "scan")
    val scanY by scanAnim.animateFloat(
        initialValue  = 0f, targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanY"
    )

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(Modifier.fillMaxSize().background(Neutral10)) {

        // Camera preview + ML Kit
        if (cameraPermission.status.isGranted && !manualInput && scannedText.isEmpty()) {
            MLKitCameraPreview(
                flashEnabled = flashEnabled,
                onContainerDetected = { number -> scannedText = number }
            )
        }

        // Dark overlay
        Box(Modifier.fillMaxSize().background(ScanOverlay))

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBackIosNew, null,
                    tint = Neutral99, modifier = Modifier.size(22.dp))
            }
            Text("Escanear Contenedor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Neutral99, modifier = Modifier.weight(1f))
            IconButton(onClick = { flashEnabled = !flashEnabled }) {
                Icon(
                    if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    null, tint = if (flashEnabled) WarningAmber else Neutral70)
            }
            IconButton(onClick = { manualInput = !manualInput }) {
                Icon(Icons.Rounded.Keyboard, null,
                    tint = if (manualInput) Emerald60 else Neutral70)
            }
        }

        // Viewfinder when scanning
        if (!manualInput && scannedText.isEmpty() && cameraPermission.status.isGranted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Apunte al número del contenedor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral80, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .width(72.dp).height(280.dp)
                        .border(2.dp, Emerald60, RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().height(2.dp)
                            .offset(y = (280 * scanY).dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Emerald60, Color.Transparent)
                                )
                            )
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Lectura automática ML Kit",
                    style = MaterialTheme.typography.labelSmall, color = Neutral60)
            }
        }

        // Permission denied state
        if (!cameraPermission.status.isGranted && !manualInput) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.NoPhotography, null,
                    tint = NeutralVar50, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("Permiso de cámara requerido",
                    style = MaterialTheme.typography.titleSmall,
                    color = Neutral80, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                ProButton("Conceder Permiso",
                    onClick  = { cameraPermission.launchPermissionRequest() },
                    icon     = Icons.Rounded.CameraAlt,
                    modifier = Modifier.fillMaxWidth())
            }
        }

        // Manual input bottom sheet
        AnimatedVisibility(
            visible  = manualInput,
            enter    = slideInVertically { it } + fadeIn(),
            exit     = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = NeutralVar20, modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.navigationBarsPadding().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Ingreso Manual",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value         = manualText,
                        onValueChange = { manualText = it.uppercase().take(11) },
                        label         = { Text("Número contenedor") },
                        placeholder   = { Text("MSCU1234567") },
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        textStyle     = containerNumberStyle.copy(fontSize = 20.sp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = Emerald60,
                            unfocusedBorderColor    = NeutralVar30,
                            cursorColor             = Emerald60,
                            focusedContainerColor   = NeutralVar20,
                            unfocusedContainerColor = NeutralVar20
                        )
                    )
                    ProButton(
                        text     = "Confirmar",
                        onClick  = {
                            if (manualText.length >= 10) {
                                scannedText = manualText
                                manualInput = false
                            }
                        },
                        icon     = Icons.Rounded.Check,
                        enabled  = manualText.length >= 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Result bottom sheet
        AnimatedVisibility(
            visible  = scannedText.isNotEmpty(),
            enter    = slideInVertically { it } + fadeIn(),
            exit     = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = NeutralVar20, modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.navigationBarsPadding().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null,
                            tint = AppColors.success, modifier = Modifier.size(20.dp))
                        Text("Contenedor Detectado",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppColors.success, fontWeight = FontWeight.SemiBold)
                    }
                    Text(scannedText, style = containerNumberStyle, color = Neutral99)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProButton(
                            text     = "Re-escanear",
                            onClick  = { scannedText = "" },
                            variant  = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                        ProButton(
                            text     = "Confirmar",
                            onClick  = {
                                vm.onContainerScanned(scannedText)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Scan.route) { inclusive = true }
                                }
                            },
                            icon     = Icons.Rounded.Check,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ── ML Kit Camera Analyzer ─────────────────────────────────
@OptIn(ExperimentalGetImage::class)
@Composable
private fun MLKitCameraPreview(
    flashEnabled       : Boolean,
    onContainerDetected: (String) -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor       = remember { Executors.newSingleThreadExecutor() }
    var camera         by remember { mutableStateOf<Camera?>(null) }

    // ML Kit recognizer (Latin script, free, no license needed)
    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    LaunchedEffect(flashEnabled) {
        camera?.cameraControl?.enableTorch(flashEnabled)
    }

    DisposableEffect(Unit) {
        onDispose {
            recognizer.close()
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = Modifier.fillMaxSize(),
        update   = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // ML Kit image analysis
                val analyzer = MlKitAnalyzer(
                    listOf(recognizer),
                    ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                    ContextCompat.getMainExecutor(context)
                ) { result ->
                    val text = result?.getValue(recognizer)?.text ?: return@MlKitAnalyzer
                    // Extract ISO 6346 container number from recognized text
                    val match = CONTAINER_REGEX.find(text.replace(" ", "").uppercase())
                    match?.value?.let { number ->
                        onContainerDetected(number)
                    }
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }

                try {
                    provider.unbindAll()
                    camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

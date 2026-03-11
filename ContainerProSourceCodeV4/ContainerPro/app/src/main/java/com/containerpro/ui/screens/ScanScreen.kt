package com.containerpro.ui.screens

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.containerpro.R
import com.containerpro.navigation.Screen
import com.containerpro.ui.theme.Emerald40
import com.containerpro.ui.theme.Emerald60
import com.containerpro.ui.theme.SuccessGreen
import com.containerpro.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import java.util.regex.Pattern

private val CONTAINER_PATTERN = Pattern.compile("[A-Z]{4}\\d{7}")

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(navController: NavController, vm: MainViewModel = viewModel()) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (!cameraPermission.status.isGranted) {
        PermissionDeniedScreen(
            onGrant   = { cameraPermission.launchPermissionRequest() },
            onManual  = { /* fall through to manual input */ },
            navController = navController,
        )
        return
    }

    CameraScanContent(navController = navController, vm = vm)
}

@Composable
private fun CameraScanContent(navController: NavController, vm: MainViewModel) {
    val context         = LocalContext.current
    val lifecycleOwner  = LocalLifecycleOwner.current

    var detectedId      by remember { mutableStateOf<String?>(null) }
    var flashEnabled    by remember { mutableStateOf(false) }
    var showManual      by remember { mutableStateOf(false) }
    var manualText      by remember { mutableStateOf("") }
    var cameraControl   by remember { mutableStateOf<Camera?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory  = { ctx ->
                PreviewView(ctx).also { previewView ->
                    startCamera(
                        context        = ctx,
                        lifecycleOwner = lifecycleOwner,
                        previewView    = previewView,
                        onTextDetected = { id ->
                            if (detectedId == null) detectedId = id
                        },
                        onCameraReady  = { cam -> cameraControl = cam },
                    )
                }
            },
        )

        // Scan frame overlay
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp, 90.dp)
                .border(2.dp, if (detectedId != null) SuccessGreen else Emerald60, RoundedCornerShape(12.dp)),
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                colors  = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(0.5f)),
            ) { Icon(Icons.Rounded.ArrowBackIosNew, null, tint = Color.White) }

            Text(
                stringResource(R.string.scan_title),
                color = Color.White, fontWeight = FontWeight.Bold,
            )

            IconButton(
                onClick = {
                    flashEnabled = !flashEnabled
                    cameraControl?.cameraControl?.enableTorch(flashEnabled)
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(0.5f)),
            ) {
                Icon(
                    if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    null, tint = Color.White,
                )
            }
        }

        // Hint text
        Text(
            stringResource(R.string.scan_hint),
            color    = Color.White.copy(alpha = 0.8f),
            style    = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.Center).padding(top = 110.dp),
        )

        // Bottom panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (detectedId != null) {
                Icon(Icons.Rounded.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(32.dp))
                Text(
                    stringResource(R.string.container_detected),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                )
                Text(
                    detectedId!!,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { detectedId = null }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.rescan))
                    }
                    Button(
                        onClick = {
                            vm.onContainerScanned(detectedId!!)
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            } else {
                if (!showManual) {
                    TextButton(onClick = { showManual = true }) {
                        Icon(Icons.Rounded.Keyboard, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.manual_input))
                    }
                } else {
                    OutlinedTextField(
                        value         = manualText,
                        onValueChange = { manualText = it.uppercase().take(11) },
                        label         = { Text(stringResource(R.string.container_number)) },
                        singleLine    = true,
                        shape         = RoundedCornerShape(14.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType   = KeyboardType.Ascii,
                        ),
                    )
                    Button(
                        onClick  = {
                            if (manualText.length == 11) {
                                vm.onContainerScanned(manualText)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = manualText.length == 11,
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun startCamera(
    context        : Context,
    lifecycleOwner : androidx.lifecycle.LifecycleOwner,
    previewView    : PreviewView,
    onTextDetected : (String) -> Unit,
    onCameraReady  : (Camera) -> Unit,
) {
    val executor        = Executors.newSingleThreadExecutor()
    val recognizer      = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val cameraFuture    = ProcessCameraProvider.getInstance(context)

    cameraFuture.addListener({
        val provider = cameraFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees,
                )
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        for (block in visionText.textBlocks) {
                            val raw = block.text.replace("\\s".toRegex(), "").uppercase()
                            val matcher = CONTAINER_PATTERN.matcher(raw)
                            if (matcher.find()) {
                                onTextDetected(matcher.group())
                                break
                            }
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }

        try {
            provider.unbindAll()
            val cam = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
            onCameraReady(cam)
        } catch (_: Exception) { /* ignore */ }
    }, ContextCompat.getMainExecutor(context))
}

@Composable
private fun PermissionDeniedScreen(
    onGrant      : () -> Unit,
    onManual     : () -> Unit,
    navController: NavController,
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.scan_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null)
                    }
                },
            )
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(Icons.Rounded.CameraAlt, null, modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.camera_permission_required),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        }
    }
}

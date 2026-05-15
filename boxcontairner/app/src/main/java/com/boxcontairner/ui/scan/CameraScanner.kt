package com.boxcontairner.ui.scan

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as GSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.boxcontairner.util.ContainerAnalyzer
import com.boxcontairner.util.ScanMode
import com.boxcontairner.util.ScanResult
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.Executors

@Composable
fun CameraScanner(
    mode: ScanMode,
    onResult: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableFloatStateOf(0f) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var focusVisible by remember { mutableStateOf(false) }

    // Línea de escaneo animada
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLine by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, label = "scanLine",
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse)
    )
    val focusAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f, label = "focusPulse",
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse)
    )

    LaunchedEffect(mode) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            val resSel = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(Size(1920, 1080), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER)
                )
                .build()

            val preview = Preview.Builder().setResolutionSelector(resSel).build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(resSel)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(cameraExecutor, ContainerAnalyzer(mode, onResult)) }

            try {
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                )
            } catch (e: Exception) {
                Timber.e(e, "Error iniciando cámara")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(torchEnabled) { camera?.cameraControl?.enableTorch(torchEnabled) }
    LaunchedEffect(zoomLevel) { camera?.cameraControl?.setLinearZoom(zoomLevel) }

    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            focusVisible = true
            delay(1500)
            focusVisible = false
        }
    }

    val modeLabel = if (mode == ScanMode.CONTAINER_ID)
        "Apunta al código ISO del contenedor" else "Apunta a la placa de datos del reefer"

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { tap ->
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(tap.x, tap.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    camera?.cameraControl?.startFocusAndMetering(action)
                    focusPoint = tap
                }
            }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val rW = w * 0.88f
            val rH = if (mode == ScanMode.CONTAINER_ID) h * 0.17f else h * 0.36f
            val l = (w - rW) / 2f
            val t = (h - rH) / 2f
            val r = l + rW
            val b = t + rH

            val dark = Color.Black.copy(alpha = 0.65f)
            drawRect(dark, topLeft = Offset(0f, 0f), size = GSize(w, t))
            drawRect(dark, topLeft = Offset(0f, b), size = GSize(w, h - b))
            drawRect(dark, topLeft = Offset(0f, t), size = GSize(l, rH))
            drawRect(dark, topLeft = Offset(r, t), size = GSize(w - r, rH))

            drawCorners(l, t, r, b, cornerPx = 60f, strokePx = 7f)

            val sy = t + rH * scanLine
            drawLine(
                color = Color(0xFF00E676).copy(alpha = 0.85f),
                start = Offset(l + 16f, sy),
                end = Offset(r - 16f, sy),
                strokeWidth = 4f
            )
            drawLine(
                color = Color(0xFF00E676).copy(alpha = 0.25f),
                start = Offset(l, sy),
                end = Offset(r, sy),
                strokeWidth = 18f
            )

            if (focusVisible && focusPoint != null) {
                val fp = focusPoint!!
                drawCircle(Color.White.copy(alpha = focusAlpha * 0.9f), radius = 40f, center = fp, style = Stroke(3f))
                drawCircle(Color.White.copy(alpha = focusAlpha * 0.4f), radius = 5f, center = fp)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(vertical = 10.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(modeLabel, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ZoomOut, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                Slider(
                    value = zoomLevel,
                    onValueChange = { zoomLevel = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF00E676),
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                Icon(Icons.Default.ZoomIn, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                Text(
                    "${(zoomLevel * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.width(32.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Toca la imagen para enfocar", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (torchEnabled) Color(0xFFFFEB3B).copy(alpha = 0.25f)
                            else Color.White.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { torchEnabled = !torchEnabled }) {
                        Icon(
                            imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (torchEnabled) Color(0xFFFFEB3B) else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCorners(l: Float, t: Float, r: Float, b: Float, cornerPx: Float, strokePx: Float) {
    val color = Color(0xFF00E676)
    drawLine(color, Offset(l, t), Offset(l + cornerPx, t), strokePx)
    drawLine(color, Offset(l, t), Offset(l, t + cornerPx), strokePx)
    drawLine(color, Offset(r, t), Offset(r - cornerPx, t), strokePx)
    drawLine(color, Offset(r, t), Offset(r, t + cornerPx), strokePx)
    drawLine(color, Offset(l, b), Offset(l + cornerPx, b), strokePx)
    drawLine(color, Offset(l, b), Offset(l, b - cornerPx), strokePx)
    drawLine(color, Offset(r, b), Offset(r - cornerPx, b), strokePx)
    drawLine(color, Offset(r, b), Offset(r, b - cornerPx), strokePx)
}

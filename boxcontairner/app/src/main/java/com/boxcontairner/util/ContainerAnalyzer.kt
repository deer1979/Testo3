package com.boxcontairner.util

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Analyzer de CameraX que aplica OCR (ML Kit on-device) y delega la interpretación
 * al scanner correspondiente según el modo.
 *
 * Throttle: 2 s entre detecciones exitosas para que el usuario tenga tiempo de leer/respirar.
 */
class ContainerAnalyzer(
    private val mode: ScanMode,
    private val onResult: (ScanResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastSuccessfulScanTime = 0L
    private val scanDelayMs = 2000L

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }

        if (System.currentTimeMillis() - lastSuccessfulScanTime < scanDelayMs) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                when (mode) {
                    ScanMode.CONTAINER_ID -> {
                        ISO6346Scanner.scan(visionText)?.let {
                            lastSuccessfulScanTime = System.currentTimeMillis()
                            onResult(ScanResult.ContainerId(it))
                        }
                    }
                    ScanMode.REEFER_PLATE -> {
                        ReeferPlateScanner.scan(visionText)?.let {
                            lastSuccessfulScanTime = System.currentTimeMillis()
                            onResult(ScanResult.ReeferInfo(it))
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

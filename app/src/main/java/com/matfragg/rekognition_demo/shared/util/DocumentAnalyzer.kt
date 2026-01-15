package com.matfragg.rekognition_demo.shared.util

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlin.collections.any

class DocumentAnalyzer(
    private val onDetectionResult: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .build()
    private val detector = ObjectDetection.getClient(options)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { objects ->
                    // 1. Verificamos si detectamos al menos un objeto
                    val detected = objects.any { obj ->
                        // 2. Lógica simple: si el objeto ocupa una parte importante del centro
                        val box = obj.boundingBox
                        val imageWidth = image.width
                        val imageHeight = image.height

                        // Calculamos si el objeto está relativamente centrado
                        val isCentered = box.centerX().toDouble() in (imageWidth * 0.3)..(imageWidth * 0.7)
                        val isLargeEnough = box.width() > (imageWidth * 0.5)

                        isCentered && isLargeEnough
                    }
                    onDetectionResult(detected)
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}
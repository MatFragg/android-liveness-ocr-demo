package com.matfragg.rekognition_demo.shared.util

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.matfragg.rekognition_demo.presentation.document_ocr.scan.ScanStep

class DocumentAnalyzer(
    private val currentStep: ScanStep,
    private val onDetectionResult: (Boolean) -> Unit,
    private val onBrightnessResult: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    private val faceDetector = FaceDetection.getClient()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_PDF417, Barcode.FORMAT_CODE_128)
            .build()
    )

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }

        // Brillo (Umbral permisivo)
        onBrightnessResult(checkBrightness(imageProxy))

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        if (currentStep == ScanStep.FRONT) {
            validateFront(image, imageProxy)
        } else {
            // 🚀 REVERSO: Ignoramos detección de objetos/rectángulos para máxima sensibilidad
            validateBack(image, imageProxy)
        }
    }

    private fun validateFront(image: InputImage, imageProxy: ImageProxy) {
        val faceTask = faceDetector.process(image)
        val textTask = textRecognizer.process(image)

        Tasks.whenAllComplete(faceTask, textTask).addOnCompleteListener {
            val hasFace = faceTask.result?.isNotEmpty() == true
            val text = textTask.result?.text?.uppercase() ?: ""
            val hasKeywords = text.contains("REPUBLICA") || text.contains("PERU")

            onDetectionResult(hasFace && hasKeywords)
            imageProxy.close()
        }
    }

    private fun validateBack(image: InputImage, imageProxy: ImageProxy) {
        val barcodeTask = barcodeScanner.process(image)
        val textTask = textRecognizer.process(image)

        // Ejecutamos ambos en paralelo para no perder frames
        Tasks.whenAllComplete(barcodeTask, textTask).addOnCompleteListener {
            val barcodes = barcodeTask.result
            val visionText = textTask.result
            val textContent = visionText?.text?.uppercase() ?: ""

            // 1. Check de Código de Barras (DNI Azul)
            val hasBarcode = barcodes?.any { it.format == Barcode.FORMAT_PDF417 } == true

            // 2. Check de MRZ (DNI Electrónico) - Buscamos patrones de flechas o "PER"
            val hasMrz = textContent.contains("<<<<") ||
                    textContent.contains("I<PER") ||
                    textContent.contains("IDPER")

            // 3. Check de Palabras Clave (Respaldo total para ambos)
            // Según tus fotos: LIMA, DIRECCION, SUFRAGIO, CONSTANCIA
            val keywords = listOf("SUFRAGIO", "CONSTANCIA", "DEPARTAMENTO", "DIRECCION", "LIMA", "PROVINCIA")
            val hasKeywords = keywords.any { textContent.contains(it) }

            // DEBUG LOG: Si esto sale en consola, el analyzer está trabajando
            Log.d("ANALYZER_BACK", "BC:$hasBarcode | MRZ:$hasMrz | KW:$hasKeywords")

            onDetectionResult(hasBarcode || hasMrz || hasKeywords)
            imageProxy.close()
        }
    }

    private fun checkBrightness(imageProxy: ImageProxy): Boolean {
        val buffer = imageProxy.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        var total = 0L
        for (b in data) total += (b.toInt() and 0xFF)
        return (total / data.size) > 235
    }
}
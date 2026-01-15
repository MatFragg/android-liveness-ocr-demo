package com.matfragg.rekognition_demo.presentation.document_ocr.scan

import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matfragg.rekognition_demo.presentation.document_ocr.components.DniPreviewStep
import com.matfragg.rekognition_demo.presentation.document_ocr.components.DocumentGuideOverlay
import com.matfragg.rekognition_demo.shared.util.DocumentAnalyzer
import com.matfragg.rekognition_demo.shared.util.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScanScreen(
    viewModel: DocumentScanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onScanComplete: (String) -> Unit // sessionId o dniNumber
) {
    val state by viewModel.state.collectAsState()
    val dniResult by viewModel.dniResult.collectAsState()

    LaunchedEffect(dniResult) {
        dniResult?.let { dni ->
            onScanComplete(dni.numeroDni)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear DNI") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.currentStep) {
                ScanStep.PREVIEW -> {
                    DniPreviewStep(
                        frontPath = state.frontImagePath,
                        backPath = state.backImagePath,
                        onConfirm = { viewModel.uploadToBackend() },
                        onRetry = { viewModel.reset() }
                    )
                }
                ScanStep.FRONT, ScanStep.BACK -> {
                    CameraView(
                        step = state.currentStep,
                        isAligned = state.isAligned, // 👈 Pasamos el estado del VM
                        onAlignmentChanged = { aligned ->
                            viewModel.setAlignment(aligned) // 👈 Pasamos la acción al VM
                        },
                        onImageCaptured = { file ->
                            if (state.currentStep == ScanStep.FRONT) {
                                viewModel.captureFrontImage(file.absolutePath)
                            } else {
                                viewModel.captureBackImage(file.absolutePath)
                            }
                        }
                    )
                }
                ScanStep.PROCESSING -> {
                    ProcessingView(
                        isLoading = state.isLoading,
                        error = state.error,
                        onRetry = { viewModel.retryCapture(ScanStep.FRONT) }
                    )
                }
                ScanStep.COMPLETE -> {
                    // Navegación automática manejada por LaunchedEffect
                }
            }
        }
    }
}

@Composable
fun CameraView(
    step: ScanStep,
    isAligned: Boolean,
    onAlignmentChanged: (Boolean) -> Unit,
    onImageCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Detectar orientación para el botón
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Listener de orientación (mantiene tu lógica actual que es correcta)
    val orientationEventListener = remember {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
    }

    LaunchedEffect(Unit) {
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder()
            .setTargetRotation(previewView.display.rotation)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(previewView.display.rotation)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(previewView.display.rotation)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(context), DocumentAnalyzer(onAlignmentChanged))
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, imageAnalysis)
    }

    DisposableEffect(Unit) { onDispose { orientationEventListener.disable() } }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        DocumentGuideOverlay(isAligned = isAligned)

        InstructionsCard(
            step = step,
            isLandscape = isLandscape,
            modifier = Modifier
                .align(if (isLandscape) Alignment.TopStart else Alignment.TopCenter)
                .padding(16.dp)
        )

        // 📸 USAMOS TU NUEVO CAPTURE BUTTON
        Box(
            modifier = Modifier
                .align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter)
                .padding(if (isLandscape) 24.dp else 32.dp)
        ) {
            CaptureButton(
                isLandscape = isLandscape,
                onClick = {
                    val fileName = if (step == ScanStep.FRONT) "dni_front.jpg" else "dni_back.jpg"

                    val photoFile = File(
                        context.externalCacheDir,
                        fileName
                    )

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                kotlin.concurrent.thread {
                                    try {
                                        // Recorta y sobrescribe el archivo específico (front o back)
                                        ImageUtils.cropImageToBoundingBox(photoFile)

                                        (context as? android.app.Activity)?.runOnUiThread {
                                            onImageCaptured(photoFile)
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("CAMERA", "Error al recortar: ${e.message}")
                                        (context as? android.app.Activity)?.runOnUiThread {
                                            onImageCaptured(photoFile)
                                        }
                                    }
                                }
                            }
                            override fun onError(e: ImageCaptureException) {
                                android.util.Log.e("CAMERA", "Error captura: ${e.message}")
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun InstructionsCard(
    step: ScanStep,
    isLandscape: Boolean, // 👈 Nuevo parámetro
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.then(
            if (isLandscape) Modifier.widthIn(max = 300.dp) else Modifier.fillMaxWidth()
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(if (isLandscape) 32.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(if (isLandscape) 8.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = if (step == ScanStep.FRONT) "Frontal DNI" else "Trasero DNI",
                    style = if (isLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // En horizontal ocultamos el subtexto para ahorrar espacio
                if (!isLandscape) {
                    Text(
                        text = "Coloca el documento dentro del recuadro",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessingView(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Procesando DNI...",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Extrayendo información",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (error != null) {
            Icon(
                Icons.Default.Error,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Error",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Text(
                error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Intentar de nuevo")
            }
        }
    }
}

@Composable
fun CaptureButton(
    isLandscape: Boolean,
    onClick: () -> Unit
) {
    // Animación de rotación del icono
    val rotationAngle by animateFloatAsState(
        targetValue = if (isLandscape) 90f else 0f,
        animationSpec = tween(400),
        label = "iconRotation"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .rotate(rotationAngle) // 👈 El icono gira, el botón se queda en su lugar
        )
    }
}
package com.matfragg.rekognition_demo.presentation.document_ocr.scan

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.matfragg.rekognition_demo.presentation.document_ocr.components.DniPreviewStep
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
    onImageCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Document Guide Overlay
        DocumentGuideOverlay(
            step = step,
            modifier = Modifier.align(Alignment.Center)
        )

        // Instructions
        InstructionsCard(
            step = step,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        // Capture Button
        FloatingActionButton(
            onClick = {
                val photoFile = File(
                    context.externalCacheDir,
                    "dni_${if (step == ScanStep.FRONT) "front" else "back"}_${System.currentTimeMillis()}.jpg"
                )

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture?.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onImageCaptured(photoFile)
                        }
                        override fun onError(exception: ImageCaptureException) {
                            // Handle error
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Default.CameraAlt,
                "Capturar",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun DocumentGuideOverlay(
    step: ScanStep,
    modifier: Modifier = Modifier
) {
    val aspectRatio = if (step == ScanStep.FRONT) 1.586f else 1.586f // DNI aspect ratio

    Canvas(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(aspectRatio)
    ) {
        drawRoundRect(
            color = Color(0x80FFFFFF),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
fun InstructionsCard(
    step: ScanStep,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (step == ScanStep.FRONT)
                        "Parte frontal del DNI"
                    else
                        "Parte trasera del DNI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Coloca el documento dentro del recuadro",
                    style = MaterialTheme.typography.bodySmall
                )
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
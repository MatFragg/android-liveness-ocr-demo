package com.matfragg.rekognition_demo.presentation.document_ocr.scan

import android.util.Log
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
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay
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
    var isTooBright by remember { mutableStateOf(false) }
    var allowManualCapture by remember { mutableStateOf(false) }

    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    // Detectar orientación
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // --- 1. LÓGICA DE CAPTURA (Extraída) ---
    val performCapture = {
        // Usamos nombres distintos para evitar que se dupliquen/sobrescriban en el preview
        val fileName = if (step == ScanStep.FRONT) "dni_front.jpg" else "dni_back.jpg"
        val photoFile = File(context.externalCacheDir, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    kotlin.concurrent.thread {
                        try {
                            // ✅ PASAMOS EL TAMAÑO DE LA VISTA PARA RECORTE PERFECTO
                            ImageUtils.cropImageToBoundingBox(
                                photoFile = photoFile,
                                viewWidth = viewSize.width,
                                viewHeight = viewSize.height,
                                isLandscape = isLandscape
                            )
                            (context as? android.app.Activity)?.runOnUiThread {
                                onImageCaptured(photoFile)
                            }
                        } catch (e: Exception) {
                            Log.e("CAMERA", "Error: ${e.message}")
                            (context as? android.app.Activity)?.runOnUiThread {
                                onImageCaptured(photoFile)
                            }
                        }
                    }
                }
                override fun onError(e: ImageCaptureException) {}
            }
        )
    }

    // --- 2. TEMPORIZADOR PARA CAPTURA MANUAL ---
    // Si en 5 segundos no detecta automáticamente, habilitamos el botón como respaldo.
    LaunchedEffect(step) {
        allowManualCapture = false
        delay(5000)
        allowManualCapture = true
    }

    // --- 3. AUTO-CAPTURA ---
    // Si el analizador da el OK (verde), disparamos solo tras 1.5s de estabilidad.
    LaunchedEffect(isAligned) {
        if (isAligned) {
            delay(1500)
            performCapture()
        }
    }

    // --- 4. CONFIGURACIÓN DE CAMERAX ---
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
        if (orientationEventListener.canDetectOrientation()) orientationEventListener.enable()
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
            // Resolución HD para que el OCR detecte las flechas <<<< del reverso
            .setTargetResolution(android.util.Size(1280, 720))
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    DocumentAnalyzer(
                        currentStep = step,
                        onDetectionResult = { aligned -> onAlignmentChanged(aligned) },
                        onBrightnessResult = { bright -> isTooBright = bright }
                    )
                )
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, imageAnalysis)
    }

    DisposableEffect(Unit) { onDispose { orientationEventListener.disable() } }

    // --- 5. INTERFAZ DE USUARIO ---
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    // 🎯 Capturamos el tamaño exacto de la pantalla
                    viewSize = coordinates.size
                }
        )

        DocumentGuideOverlay(isAligned = isAligned)

        InstructionsCard(
            step = step,
            isLandscape = isLandscape,
            isAligned = isAligned,
            isTooBright = isTooBright,
            modifier = Modifier
                .align(if (isLandscape) Alignment.TopStart else Alignment.TopCenter)
                .padding(16.dp)
        )

        // Botón de captura dinámico
        Box(
            modifier = Modifier
                .align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter)
                .padding(if (isLandscape) 24.dp else 32.dp)
        ) {
            CaptureButton(
                isLandscape = isLandscape,
                // Habilitado si está en verde O si ya pasaron los 5 segundos de espera
                enabled = isAligned || allowManualCapture,
                isManualForced = allowManualCapture && !isAligned,
                onClick = { performCapture() }
            )
        }

        // Mensaje de ayuda para modo manual
        if (allowManualCapture && !isAligned) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "No se detecta el reverso. Intenta capturar manualmente.",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
@Composable
fun InstructionsCard(
    step: ScanStep,
    isLandscape: Boolean,
    isAligned: Boolean,
    isTooBright: Boolean, // 👈 Nuevo parámetro
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isTooBright -> Color(0xFFFFF3E0).copy(alpha = 0.9f) // Naranja claro para aviso
        isAligned -> Color(0xFFE8F5E9).copy(alpha = 0.9f)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    }

    val contentColor = when {
        isTooBright -> Color(0xFFE65100) // Naranja oscuro
        isAligned -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.then(
            if (isLandscape) Modifier.widthIn(max = 280.dp) else Modifier.fillMaxWidth()
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(if (isLandscape) 24.dp else 12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when {
                    isTooBright -> Icons.Default.LightMode
                    isAligned -> Icons.Default.CheckCircle
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = contentColor
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isTooBright) "¡Mucha Luz!" else if (step == ScanStep.FRONT) "Anverso" else "Reverso",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (isTooBright) "Evita los reflejos sobre el DNI"
                    else if (isAligned) "¡Listo!"
                    else "Alinea el documento",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
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

@Composable
fun CaptureButton(
    isLandscape: Boolean,
    enabled: Boolean,
    isManualForced: Boolean, // Nuevo: Indica si la captura es manual por timeout
    onClick: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isLandscape) 90f else 0f,
        animationSpec = tween(400),
        label = "iconRotation"
    )

    val containerColor = when {
        isManualForced -> MaterialTheme.colorScheme.tertiary // Color de advertencia/manual
        enabled -> MaterialTheme.colorScheme.primary
        else -> Color.Gray
    }

    FloatingActionButton(
        onClick = { if (enabled) onClick() },
        modifier = Modifier
            .size(72.dp)
            .alpha(if (enabled) 1f else 0.5f),
        containerColor = containerColor
    ) {
        Icon(
            imageVector = if (isManualForced) Icons.Default.Camera else if (enabled) Icons.Default.CameraAlt else Icons.Default.FilterCenterFocus,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .rotate(rotationAngle)
        )
    }
}
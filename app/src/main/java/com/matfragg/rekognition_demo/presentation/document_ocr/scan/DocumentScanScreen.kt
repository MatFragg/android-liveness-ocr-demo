package com.matfragg.rekognition_demo.presentation.document_ocr.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    onScanComplete: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val dniResult by viewModel.dniResult.collectAsState()

    // Manejo de permisos
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        viewModel.onCameraPermissionResult(isGranted)
    }

    LaunchedEffect(dniResult) {
        dniResult?.let { dni -> onScanComplete(dni.numeroDni) }
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                    if (hasCameraPermission) {
                        CameraView(
                            step = state.currentStep,
                            isAligned = state.isAligned,
                            onAlignmentChanged = { aligned -> viewModel.setAlignment(aligned) },
                            onImageCaptured = { file ->
                                if (state.currentStep == ScanStep.FRONT) {
                                    viewModel.captureFrontImage(file.absolutePath)
                                } else {
                                    viewModel.captureBackImage(file.absolutePath)
                                }
                            }
                        )
                    } else {
                        PermissionRequiredView(onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        })
                    }
                }
                ScanStep.PROCESSING -> {
                    ProcessingView(
                        isLoading = state.isLoading,
                        error = state.error,
                        onRetry = { viewModel.retryCapture(ScanStep.FRONT) }
                    )
                }
                ScanStep.COMPLETE -> { /* Transición manejada por el Navegador */ }
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

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val performCapture = {
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
                            ImageUtils.cropImageToBoundingBox(
                                photoFile = photoFile,
                                viewWidth = viewSize.width,
                                viewHeight = viewSize.height,
                                isLandscape = isLandscape
                            )
                            (context as? android.app.Activity)?.runOnUiThread { onImageCaptured(photoFile) }
                        } catch (e: Exception) {
                            Log.e("CAMERA", "Error de recorte: ${e.message}")
                            (context as? android.app.Activity)?.runOnUiThread { onImageCaptured(photoFile) }
                        }
                    }
                }
                override fun onError(e: ImageCaptureException) {
                    Log.e("CAMERA", "Error de captura: ${e.message}")
                }
            }
        )
    }

    LaunchedEffect(step) {
        allowManualCapture = false
        delay(5000)
        allowManualCapture = true
    }

    LaunchedEffect(isAligned) {
        if (isAligned) {
            delay(1500)
            performCapture()
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize().onGloballyPositioned { viewSize = it.size }
        )

        DocumentGuideOverlay(isAligned = isAligned)

        InstructionsCard(
            step = step,
            isLandscape = isLandscape,
            isAligned = isAligned,
            isTooBright = isTooBright,
            modifier = Modifier.align(if (isLandscape) Alignment.TopStart else Alignment.TopCenter).padding(16.dp)
        )

        Box(modifier = Modifier.align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter).padding(if (isLandscape) 24.dp else 32.dp)) {
            CaptureButton(
                isLandscape = isLandscape,
                enabled = isAligned || allowManualCapture,
                isManualForced = allowManualCapture && !isAligned,
                onClick = { performCapture() }
            )
        }
    }
}

@Composable
fun PermissionRequiredView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Permiso de cámara necesario", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text("Necesitamos acceso a la cámara para escanear el DNI y validar tu identidad.", textAlign = TextAlign.Center, modifier = Modifier.alpha(0.7f))
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) { Text("Conceder Permiso") }
    }
}

@Composable
fun InstructionsCard(step: ScanStep, isLandscape: Boolean, isAligned: Boolean, isTooBright: Boolean, modifier: Modifier = Modifier) {
    val containerColor = when {
        isTooBright -> Color(0xFFFFF3E0).copy(alpha = 0.9f)
        isAligned -> Color(0xFFE8F5E9).copy(alpha = 0.9f)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    }

    Card(
        modifier = modifier.then(if (isLandscape) Modifier.widthIn(max = 280.dp) else Modifier.fillMaxWidth()),
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
                tint = if (isTooBright) Color(0xFFE65100) else if (isAligned) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = if (isTooBright) "¡Mucha Luz!" else if (step == ScanStep.FRONT) "Anverso" else "Reverso", fontWeight = FontWeight.Bold)
                Text(text = if (isTooBright) "Evita los reflejos" else if (isAligned) "¡Listo!" else "Alinea el DNI", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ProcessingView(isLoading: Boolean, error: String?, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
            Spacer(Modifier.height(24.dp))
            Text("Procesando...", style = MaterialTheme.typography.titleLarge)
        } else if (error != null) {
            Icon(Icons.Default.Error, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Text(error, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onRetry) { Text("Intentar de nuevo") }
        }
    }
}

@Composable
fun CaptureButton(isLandscape: Boolean, enabled: Boolean, isManualForced: Boolean, onClick: () -> Unit) {
    val rotationAngle by animateFloatAsState(targetValue = if (isLandscape) 90f else 0f, animationSpec = tween(400), label = "")
    FloatingActionButton(
        onClick = { if (enabled) onClick() },
        modifier = Modifier.size(72.dp).alpha(if (enabled) 1f else 0.5f),
        containerColor = if (isManualForced) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = if (isManualForced) Icons.Default.Camera else Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(32.dp).rotate(rotationAngle)
        )
    }
}
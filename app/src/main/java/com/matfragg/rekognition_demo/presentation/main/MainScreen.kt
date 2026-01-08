package com.matfragg.rekognition_demo.presentation.main

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToLiveness: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val result by viewModel.result.collectAsState()
    val context = LocalContext.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var captureImageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Animación para el botón de captura
    val infiniteTransition = rememberInfiniteTransition(label = "capture")
    val captureScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "captureScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AWS Rekognition",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF121212))
        ) {
            // Camera Preview with Face Overlay
            CameraPreviewWithOverlay(
                lensFacing = lensFacing,
                onImageCaptureReady = { imageCapture ->
                    captureImageCapture = imageCapture
                }
            )

            // Top Card: Mode Switch & Status
            TopControlsCard(
                state = state,
                onToggleMode = { viewModel.toggleMode() },
                onNavigateToLiveness = onNavigateToLiveness,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            // Bottom Controls
            BottomControls(
                lensFacing = lensFacing,
                onSwitchCamera = { /* ... */ },
                onCapture = {
                    captureImageCapture?.let { capture ->
                        // PASAMOS EL CONTEXT AQUÍ
                        capturePhoto(capture, context) { file ->
                            viewModel.capturePhoto(file)
                        }
                    }
                },
                captureScale = captureScale,
                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)
            )

            // Result Dialog
            result?.let { res ->
                EnhancedResultDialog(
                    result = res,
                    onDismiss = { viewModel.clearResult() }
                )
            }
        }
    }
}

@Composable
fun TopControlsCard(
    state: MainState,
    onToggleMode: () -> Unit,
    onNavigateToLiveness: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Liveness Button
            Button(
                onClick = onNavigateToLiveness,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF03DAC5)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRUEBA DE VIDA",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Divider(color = Color(0x33FFFFFF))

            // Mode Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.isComparisonMode) "Comparación" else "Detección",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (state.isComparisonMode) "2 fotos" else "1 foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBB86FC)
                    )
                }

                Switch(
                    checked = state.isComparisonMode,
                    onCheckedChange = { onToggleMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFBB86FC),
                        checkedTrackColor = Color(0x77BB86FC)
                    )
                )
            }

            // Status Message
            AnimatedStatusMessage(message = state.statusMessage)
        }
    }
}

@Composable
fun AnimatedStatusMessage(message: String) {
    val animatedColor by animateColorAsState(
        targetValue = when {
            "capturada" in message.lowercase() -> Color(0xFF4CAF50)
            "error" in message.lowercase() -> Color(0xFFF44336)
            else -> Color(0xFFBB86FC)
        },
        label = "statusColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(animatedColor.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = animatedColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = animatedColor
        )
    }
}

@Composable
fun BottomControls(
    lensFacing: Int,
    onSwitchCamera: () -> Unit,
    onCapture: () -> Unit,
    captureScale: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Switch Camera Button
        FloatingActionButton(
            onClick = onSwitchCamera,
            containerColor = Color(0xFF1E1E1E),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Cambiar cámara"
            )
        }

        // Capture Button
        FloatingActionButton(
            onClick = onCapture,
            modifier = Modifier
                .size(72.dp)
                .scale(captureScale),
            containerColor = Color(0xFFBB86FC),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capturar",
                modifier = Modifier.size(32.dp)
            )
        }

        // Spacer for symmetry
        Spacer(modifier = Modifier.size(56.dp))
    }
}

@Composable
fun CameraPreviewWithOverlay(
    lensFacing: Int,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        onImageCaptureReady(imageCapture)

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        FaceOverlay(
            modifier = Modifier
                .width(280.dp)
                .height(400.dp)
                .align(Alignment.Center)
        )

        ScanLineAnimation(
            modifier = Modifier
                .width(280.dp)
                .height(3.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun FaceOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRoundRect(
            color = Color(0x80BB86FC),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(32.dp.toPx()),
            style = Stroke(width = 5.dp.toPx())
        )
    }
}

@Composable
fun ScanLineAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanOffset"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFBB86FC),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun EnhancedResultDialog(
    result: FaceResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            val icon = when (result) {
                is FaceResult.Detection -> Icons.Default.Face
                is FaceResult.Comparison -> Icons.Default.CompareArrows
                is FaceResult.Error -> Icons.Default.Error
            }
            Icon(icon, null, modifier = Modifier.size(48.dp))
        },
        title = {
            Text(
                "Resultado AWS",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (result) {
                        is FaceResult.Detection -> {
                            ResultItem("🎯 Confianza", "${String.format("%.2f", result.analysis.confidence)}%")
                            ResultItem("🧬 Estado", if (result.analysis.isAlive) "REAL ✅" else "FALSO ❌")
                            result.analysis.emotions?.let {
                                ResultItem("🎭 Emociones", it.joinToString { e -> e.displayName })
                            }
                        }
                        is FaceResult.Comparison -> {
                            ResultItem("📊 Similitud", "${String.format("%.2f", result.comparison.similarity)}%")
                            ResultItem("✨ Resultado", if (result.comparison.isMatch()) "COINCIDE ✅" else "NO COINCIDE ❌")
                            ResultItem("🛡️ Estado", result.comparison.status.name)
                        }
                        is FaceResult.Error -> {
                            Text(
                                "❌ ${result.message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("ACEPTAR")
            }
        }
    )
}

@Composable
fun ResultItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun capturePhoto(
    imageCapture: ImageCapture,
    context: android.content.Context,
    onImageCaptured: (File) -> Unit
) {
    // Crear el archivo temporal para la foto
    val photoFile = File(
        context.getExternalFilesDir(null),
        "photo_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Notificar al ViewModel que la imagen está lista
                onImageCaptured(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                android.util.Log.e("CAMERA", "Error al capturar foto: ${exception.message}", exception)
            }
        }
    )
}
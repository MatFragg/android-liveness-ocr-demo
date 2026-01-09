package com.matfragg.rekognition_demo.shared.util

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matfragg.rekognition_demo.presentation.main.MainScreen
import com.matfragg.rekognition_demo.presentation.main.MainViewModel

@Composable
fun rememberCameraPermissionState(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): CameraPermissionState {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    return CameraPermissionState(
        hasPermission = permissionGranted,
        requestPermission = { launcher.launch(Manifest.permission.CAMERA) }
    )
}

data class CameraPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

// Uso en MainScreen:
@Composable
fun MainScreenWithPermissions(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToLiveness: () -> Unit,
    onNavigateToFaceRecognition: () -> Unit,
    onNavigateToDocumentScan: () -> Unit
) {
    var showRationale by remember { mutableStateOf(false) }

    val permissionState = rememberCameraPermissionState(
        onPermissionGranted = {
            // Permiso otorgado, mostrar UI normal
        },
        onPermissionDenied = {
            showRationale = true
        }
    )

    if (!permissionState.hasPermission) {
        // Mostrar pantalla de solicitud de permisos
        CameraPermissionScreen(
            onRequestPermission = permissionState.requestPermission
        )
    } else {
        // Mostrar la UI normal
        MainScreen(
            viewModel = viewModel,
            onNavigateToLiveness = onNavigateToLiveness,
            onNavigateToFaceRecognition = onNavigateToFaceRecognition,
            onNavigateToDocumentScan = onNavigateToDocumentScan
        )
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permiso de Cámara Requerido") },
            text = {
                Text("Esta app necesita acceso a la cámara para capturar imágenes de rostros.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionState.requestPermission()
                }) {
                    Text("Otorgar Permiso")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CameraPermissionScreen(
    onRequestPermission: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material.icons.Icons
        androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = androidx.compose.ui.Modifier.size(64.dp),
            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

        androidx.compose.material3.Text(
            text = "Permiso de Cámara Requerido",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

        androidx.compose.material3.Text(
            text = "Esta app necesita acceso a tu cámara para capturar y analizar imágenes de rostros.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(32.dp))

        androidx.compose.material3.Button(
            onClick = onRequestPermission,
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text("Otorgar Permiso de Cámara")
        }
    }
}
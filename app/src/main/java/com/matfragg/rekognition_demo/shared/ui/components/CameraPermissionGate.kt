package com.matfragg.rekognition_demo.shared.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun CameraPermissionGate(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    // Launcher para solicitar el permiso al SO
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onPermissionGranted() else onPermissionDenied()
    }

    // Función que la UI llamará cuando el usuario haga clic en "Capturar"
    val checkAndRequestPermission = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    content(checkAndRequestPermission)
}
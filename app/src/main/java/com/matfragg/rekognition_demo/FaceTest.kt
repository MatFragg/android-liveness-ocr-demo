package com.matfragg.rekognition_demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.amplifyframework.ui.liveness.ui.FaceLivenessDetector

class CustomFaceLivenessActivity : ComponentActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        val sessionId = intent.getStringExtra("SESSION_ID") ?: ""
        val region = intent.getStringExtra("REGION") ?: "us-east-1"
        val cameraPreference = intent.getStringExtra("CAMERA_PREFERENCE") ?: "front"

        setContent {
            MaterialTheme {
                var showWarning by remember { mutableStateOf(cameraPreference == "back") }
                var proceedWithLiveness by remember { mutableStateOf(cameraPreference == "front") }

                when {
                    showWarning -> {
                        BackCameraWarningScreen(
                            onProceed = {
                                showWarning = false
                                proceedWithLiveness = true
                            },
                            onCancel = {
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        )
                    }
                    proceedWithLiveness -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (cameraPreference == "back") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "⚠️ Usando cámara trasera - Resultados pueden variar",
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            FaceLivenessDetector(
                                sessionId = sessionId,
                                region = region,
                                onComplete = {
                                    setResult(RESULT_OK)
                                    finish()
                                },
                                onError = { error ->
                                    Log.e("LIVENESS", "Error: ${error.message}")
                                    showErrorDialog(error.message ?: "Error desconocido")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Error de Face Liveness")
                .setMessage(message)
                .setPositiveButton("Cerrar") { _, _ ->
                    setResult(RESULT_CANCELED)
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }
}

@Composable
fun BackCameraWarningScreen(
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Warning,
            contentDescription = "Advertencia",
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "⚠️ ADVERTENCIA IMPORTANTE",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AWS Face Liveness NO está diseñado para cámara trasera:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• La detección probablemente FALLARÁ\n" +
                            "• El componente usará su cámara por defecto (frontal)\n" +
                            "• No hay garantía de que la cámara trasera funcione\n" +
                            "• Los resultados no serán confiables",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Nota: El componente FaceLivenessDetector de AWS está diseñado exclusivamente para selfies con cámara frontal. No hay una forma oficial de cambiar a cámara trasera.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = onProceed,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Probar de todas formas")
            }
        }
    }
}
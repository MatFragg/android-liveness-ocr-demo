package com.matfragg.rekognition_demo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amplifyframework.ui.liveness.ui.Camera
import com.amplifyframework.ui.liveness.ui.ChallengeOptions
import com.amplifyframework.ui.liveness.ui.FaceLivenessDetector
import com.amplifyframework.ui.liveness.ui.LivenessChallenge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

class FaceLivenessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialSessionId = intent.getStringExtra("SESSION_ID") ?: ""
        val region = intent.getStringExtra("REGION") ?: "us-east-1"
        val cameraPreference = intent.getStringExtra("CAMERA_PREFERENCE") ?: "front"

        setContent {
            MaterialTheme {
                LivenessScreen(
                    initialSessionId = initialSessionId,
                    initialCamera = if (cameraPreference == "back") Camera.Back else Camera.Front,
                    region = region,
                    onComplete = {
                        setResult(RESULT_OK)
                        finish()
                    },
                    onError = { error ->
                        showErrorAndExit(error)
                    },
                    onCancel = {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    private fun showErrorAndExit(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error de Liveness")
            .setMessage(message)
            .setPositiveButton("Cerrar") { _, _ ->
                setResult(RESULT_CANCELED)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}

@Composable
fun LivenessScreen(
    initialSessionId: String,
    initialCamera: Camera,
    region: String,
    onComplete: () -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit
) {
    var currentSessionId by remember { mutableStateOf(initialSessionId) }
    var currentCamera by remember { mutableStateOf(initialCamera) }
    var isLoading by remember { mutableStateOf(false) }
    var showCameraSwitch by remember { mutableStateOf(true) }
    var livenessKey by remember { mutableStateOf(0) } // ⬅️ NUEVO: Key única para forzar recomposición

    val scope = rememberCoroutineScope()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Creando nueva sesión...")
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showCameraSwitch) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cámara: ${if (currentCamera == Camera.Back) "Trasera 📷" else "Frontal 🤳"}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                scope.launch {
                                    val newSessionId = createNewLivenessSession()
                                    if (newSessionId != null) {
                                        currentSessionId = newSessionId
                                        currentCamera = if (currentCamera == Camera.Back)
                                            Camera.Front
                                        else
                                            Camera.Back
                                        livenessKey++ // ⬅️ NUEVO: Incrementar key para forzar recreación
                                        isLoading = false
                                    } else {
                                        isLoading = false
                                        onError("Error al crear nueva sesión")
                                    }
                                }
                            }
                        ) {
                            Text("Cambiar cámara")
                        }
                    }
                }
            }

            // ⬅️ CRÍTICO: Usar key() para forzar que se recree el componente
            key(livenessKey) {
                FaceLivenessDetector(
                    sessionId = currentSessionId,
                    region = region,
                    challengeOptions = ChallengeOptions(
                        faceMovement = LivenessChallenge.FaceMovement(camera = currentCamera)
                    ),
                    onComplete = {
                        Log.i("LIVENESS", "Completado con éxito")
                        showCameraSwitch = false
                        onComplete()
                    },
                    onError = { error ->
                        Log.e("LIVENESS", "Error: ${error.message}")
                        onError(error.message ?: "Error desconocido")
                    }
                )
            }
        }
    }
}

suspend fun createNewLivenessSession(): String? = withContext(Dispatchers.IO) {
    var conn: HttpURLConnection? = null
    try {
        val jsonRequest = JSONObject()
        jsonRequest.put("mode", "create_liveness_session")

        val url = URL("https://oj6hcp7azmemswthc5qicp4eom0hbgkl.lambda-url.us-east-2.on.aws/")
        conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 20000

        conn.outputStream.write(jsonRequest.toString().toByteArray())

        if (conn.responseCode == 200) {
            val inputStream: InputStream = conn.inputStream
            val scanner = Scanner(inputStream).useDelimiter("\\A")
            val response = if (scanner.hasNext()) scanner.next() else "{}"

            val jsonResponse = JSONObject(response)
            jsonResponse.getString("sessionId")
        } else {
            Log.e("LIVENESS", "Error al crear sesión: ${conn.responseCode}")
            null
        }
    } catch (e: Exception) {
        Log.e("LIVENESS", "Excepción al crear sesión", e)
        null
    } finally {
        conn?.disconnect()
    }
}
package com.matfragg.rekognition_demo.presentation.face_recognition

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matfragg.rekognition_demo.domain.auth.usecase.LoginUseCase
import com.matfragg.rekognition_demo.domain.face_recognition.usecase.CompareFacesUseCase
import com.matfragg.rekognition_demo.domain.face_recognition.usecase.DetectFaceUseCase
import com.matfragg.rekognition_demo.shared.domain.Result
import com.matfragg.rekognition_demo.shared.util.Constants
import com.matfragg.rekognition_demo.shared.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FaceRecognitionViewModel @Inject constructor(
    private val detectFaceUseCase: DetectFaceUseCase,
    private val compareFacesUseCase: CompareFacesUseCase,
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(FaceRecognitionState())
    val state: StateFlow<FaceRecognitionState> = _state.asStateFlow()

    private val _result = MutableStateFlow<FaceResult?>(null)
    val result: StateFlow<FaceResult?> = _result.asStateFlow()
    fun toggleMode() {
        _state.value = _state.value.copy(
            isComparisonMode = !_state.value.isComparisonMode,
            photo1 = null,
            photo2 = null,
            statusMessage = if (!_state.value.isComparisonMode)
                "Listo para comparar rostros"
            else
                "Listo para analizar rostro"
        )
    }


    fun capturePhoto(file: File) {
        if (_state.value.isComparisonMode) {
            if (_state.value.photo1 == null) {
                _state.value = _state.value.copy(
                    photo1 = file,
                    statusMessage = "Foto 1 capturada. Capture la Foto 2."
                )
            } else {
                _state.value = _state.value.copy(photo2 = file)
                compareFaces(_state.value.photo1!!, file)
            }
        } else {
            detectFace(file)
        }
    }

    private fun detectFace(file: File) {
        viewModelScope.launch {
            _state.value = _state.value.copy(statusMessage = "Analizando rostro...")

            when (val result = detectFaceUseCase(file)) {
                is Result.Success -> {
                    _result.value = FaceResult.Detection(result.data)
                    _state.value = _state.value.copy(statusMessage = "Análisis completado")
                }
                is Result.Error -> {
                    _result.value = FaceResult.Error(result.exception.message ?: "Error desconocido")
                    _state.value = _state.value.copy(statusMessage = "Error al analizar")
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun compareFaces(source: File, target: File) {
        viewModelScope.launch {
            _state.update { it.copy(statusMessage = "Verificando credenciales...") }

            // 1. ensureAuthenticated hace el login y guarda el token
            if (!ensureAuthenticated()) return@launch

            // 2. Aquí el token YA ESTÁ en el Singleton de TokenManager
            _state.update { it.copy(statusMessage = "Comparando rostros...") }

            when (val result = compareFacesUseCase(source, target)) {
                is Result.Success -> {
                    _result.value = FaceResult.Comparison(result.data)
                    _state.update { it.copy(statusMessage = "Comparación completada") }
                }
                is Result.Error -> {
                    // Si el error es 401, aquí lo verás
                    Log.e("API_ERROR", "Error: ${result.exception.message}")
                    _result.value = FaceResult.Error(result.exception.message ?: "Error en API")
                    _state.update { it.copy(statusMessage = "Error al comparar") }
                    compareFacesWithRetry(source, target)
                }
                is Result.Loading -> {}
            }
        }
    }

    private suspend fun compareFacesWithRetry(source: File, target: File) {
        var attempt = 0
        val maxAttempts = 2

        while (attempt < maxAttempts) {
            attempt++
            Log.d("COMPARE", "Intento $attempt de $maxAttempts")

            if (!ensureAuthenticated()) {
                Log.e("COMPARE", "Fallo en autenticación")
                return
            }

            _state.update { it.copy(statusMessage = "Comparando rostros...") }

            when (val result = compareFacesUseCase(source, target)) {
                is Result.Success -> {
                    _result.value = FaceResult.Comparison(result.data)
                    _state.update { it.copy(statusMessage = "Comparación completada") }
                    return
                }
                is Result.Error -> {
                    if (result.exception.message?.contains("401") == true && attempt < maxAttempts) {
                        Log.w("COMPARE", "401 recibido, reintentando con nuevo token...")
                        tokenManager.clearToken() // Forzar nuevo login
                        kotlinx.coroutines.delay(500)
                    } else {
                        _result.value = FaceResult.Error(result.exception.message ?: "Error")
                        _state.update { it.copy(statusMessage = "Error al comparar") }
                        return
                    }
                }
                else -> {}
            }
        }
    }

    private suspend fun ensureAuthenticated(): Boolean {
        val token = tokenManager.getToken()

        if (token.isNullOrBlank() || tokenManager.isTokenExpired()) {
            Log.d("AUTH_FLOW", "🔐 Iniciando login...")
            _state.update { it.copy(statusMessage = "Autenticando...") }

            val loginResult = loginUseCase(
                Constants.ACJ_API_CLIENT_ID,
                Constants.ACJ_API_CLIENT_SECRET
            )

            return when (loginResult) {
                is Result.Success -> {
                    // Guardar token
                    tokenManager.saveToken(loginResult.data.accessToken)
                    tokenManager.saveExpiry(loginResult.data.expireIn)

                    // ✅ CRUCIAL: Pequeño delay para asegurar propagación
                    kotlinx.coroutines.delay(100)

                    Log.d("AUTH_FLOW", "✅ Token guardado y listo")
                    true
                }
                is Result.Error -> {
                    Log.e("AUTH_FLOW", "❌ Error en login: ${loginResult.exception.message}")
                    _result.value = FaceResult.Error("Error de autenticación")
                    _state.update { it.copy(statusMessage = "Error de autenticación") }
                    false
                }
                else -> false
            }
        }

        Log.d("AUTH_FLOW", "✅ Token ya existe, continuando...")
        return true
    }

    fun clearResult() {
        _result.value = null
        _state.value = _state.value.copy(
            photo1 = null,
            photo2 = null,
            statusMessage = if (_state.value.isComparisonMode)
                "Listo para comparar rostros"
            else
                "Listo para analizar rostro"
        )
    }
}

sealed class FaceResult {
    data class Detection(val analysis: com.matfragg.rekognition_demo.domain.face_recognition.model.FaceAnalysis) : FaceResult()
    data class Comparison(val comparison: com.matfragg.rekognition_demo.domain.face_recognition.model.FaceComparison) : FaceResult()
    data class Error(val message: String) : FaceResult()
}
package com.matfragg.rekognition_demo.presentation.liveness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matfragg.rekognition_demo.domain.liveness.model.CameraMode
import com.matfragg.rekognition_demo.domain.liveness.usecase.CreateLivenessSessionUseCase
import com.matfragg.rekognition_demo.domain.liveness.usecase.GetLivenessResultUseCase
import com.matfragg.rekognition_demo.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LivenessViewModel @Inject constructor(
    private val createSessionUseCase: CreateLivenessSessionUseCase,
    private val getResultUseCase: GetLivenessResultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LivenessState())
    val state: StateFlow<LivenessState> = _state.asStateFlow()

    init {
        createSession()
    }

    fun createSession(camera: CameraMode = CameraMode.FRONT) {
        // 🛡️ ESTA LÍNEA DETIENE EL BUCLE:
        // Si ya tenemos un ID de sesión o estamos cargando, ignoramos cualquier otra llamada.
        if (_state.value.sessionId != null || _state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, currentCamera = camera) }

            when (val result = createSessionUseCase()) {
                is Result.Success -> {
                    _state.update { it.copy(
                        sessionId = result.data.sessionId,
                        isLoading = false
                    )}
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        error = "Error: ${result.exception.message}",
                        isLoading = false
                    )}
                }
                else -> {}
            }
        }
    }

    fun switchCamera() {
        val newCamera = if (_state.value.currentCamera == CameraMode.FRONT)
            CameraMode.BACK
        else
            CameraMode.FRONT

        createSession(newCamera)
    }

    fun onLivenessComplete(sessionId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isCompleted = true,
                showCameraSwitch = false
            )

            // Obtener resultado
            when (val result = getResultUseCase(sessionId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        result = result.data
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        error = "Error al obtener resultado: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onError(message: String) {
        _state.value = _state.value.copy(
            error = message,
            isLoading = false
        )
    }
}
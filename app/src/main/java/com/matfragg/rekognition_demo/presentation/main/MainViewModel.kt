package com.matfragg.rekognition_demo.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matfragg.rekognition_demo.domain.face_recognition.usecase.CompareFacesUseCase
import com.matfragg.rekognition_demo.domain.face_recognition.usecase.DetectFaceUseCase
import com.matfragg.rekognition_demo.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val detectFaceUseCase: DetectFaceUseCase,
    private val compareFacesUseCase: CompareFacesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

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
            _state.value = _state.value.copy(statusMessage = "Comparando rostros...")

            when (val result = compareFacesUseCase(source, target)) {
                is Result.Success -> {
                    _result.value = FaceResult.Comparison(result.data)
                    _state.value = _state.value.copy(statusMessage = "Comparación completada")
                }
                is Result.Error -> {
                    _result.value = FaceResult.Error(result.exception.message ?: "Error desconocido")
                    _state.value = _state.value.copy(statusMessage = "Error al comparar")
                }
                is Result.Loading -> {}
            }
        }
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
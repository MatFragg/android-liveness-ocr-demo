package com.matfragg.rekognition_demo.presentation.document_ocr.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import com.matfragg.rekognition_demo.domain.document_ocr.usecase.ProcessDniUseCase
import com.matfragg.rekognition_demo.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DocumentScanViewModel @Inject constructor(
    private val processDniUseCase: ProcessDniUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentScanState())
    val state: StateFlow<DocumentScanState> = _state.asStateFlow()

    private val _dniResult = MutableStateFlow<DniData?>(null)
    val dniResult: StateFlow<DniData?> = _dniResult.asStateFlow()

    fun captureFrontImage(imagePath: String) {
        _state.value = _state.value.copy(
            frontImagePath = imagePath,
            currentStep = ScanStep.BACK
        )
    }

    fun captureBackImage(imagePath: String) {
        _state.value = _state.value.copy(
            backImagePath = imagePath,
            currentStep = ScanStep.PREVIEW
        )
    }

    fun uploadToBackend() {
        val frontPath = _state.value.frontImagePath
        val backPath = _state.value.backImagePath

        // 1. Verificación con Log para depurar
        if (frontPath == null || backPath == null) {
            _state.value = _state.value.copy(error = "Faltan las imágenes del DNI")
            return
        }

        viewModelScope.launch {
            // 2. CAMBIO CRÍTICO: Cambiar a ScanStep.PROCESSING para que la UI muestre el spinner
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                currentStep = ScanStep.PROCESSING // <-- Agrega esto
            )

            android.util.Log.d("SCAN_VM", "Iniciando subida... Front: $frontPath")

            // 3. Ejecutar el UseCase
            when (val result = processDniUseCase(File(frontPath), File(backPath))) {
                is Result.Success -> {
                    android.util.Log.d("SCAN_VM", "Subida exitosa: ${result.data.numeroDni}")
                    _dniResult.value = result.data
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentStep = ScanStep.COMPLETE
                    )
                }
                is Result.Error -> {
                    android.util.Log.e("SCAN_VM", "Error en subida: ${result.exception.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentStep = ScanStep.PROCESSING, // Mantener aquí para mostrar el botón de reintentar
                        error = result.exception.message ?: "Error al procesar DNI"
                    )
                }
                is Result.Loading -> {
                    // El estado de carga ya lo manejamos arriba
                }
            }
        }
    }

    fun retryCapture(step: ScanStep) {
        _state.value = _state.value.copy(
            currentStep = step,
            error = null
        )
    }

    fun reset() {
        _state.value = DocumentScanState()
        _dniResult.value = null
    }
}
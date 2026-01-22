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
import kotlinx.coroutines.flow.update
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

        if (frontPath == null || backPath == null) {
            _state.value = _state.value.copy(error = "Faltan las imágenes del DNI")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentStep = ScanStep.PROCESSING) }

            when (val result = processDniUseCase(File(frontPath), File(backPath))) {
                is Result.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
                is Result.Success -> {
                    _dniResult.value = result.data
                    _state.update { it.copy(isLoading = false, currentStep = ScanStep.COMPLETE) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.exception.message) }
                }
            }
        }
    }

    fun setAlignment(isAligned: Boolean) {
        if (_state.value.isAligned != isAligned) {
            _state.value = _state.value.copy(isAligned = isAligned)
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

    fun onCameraPermissionResult(isGranted: Boolean) {
        _state.update { it.copy(hasCameraPermission = isGranted) }
    }
}
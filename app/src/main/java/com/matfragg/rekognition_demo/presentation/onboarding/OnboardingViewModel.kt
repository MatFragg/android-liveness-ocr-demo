package com.matfragg.rekognition_demo.presentation.onboarding

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import com.matfragg.rekognition_demo.domain.face_recognition.usecase.CompareFacesUseCase
import com.matfragg.rekognition_demo.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val compareFacesUseCase: CompareFacesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    fun onDniCaptured(data: DniData) {
        _state.update { it.copy(dniData = data) }
    }

    fun onLivenessCompleted(base64Photo: String) {
        _state.update { it.copy(livenessPhotoBase64 = base64Photo) }
    }

    fun compareFacial() {
        val dniB64 = _state.value.dniData?.fotoPersonaBase64 ?: return
        val liveB64 = _state.value.livenessPhotoBase64 ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val dniFile = base64ToFile(dniB64, "dni_temp.jpg")
                val liveFile = base64ToFile(liveB64, "live_temp.jpg")

                when (val result = compareFacesUseCase(dniFile, liveFile)) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(comparisonResult = result.data, isLoading = false)
                        }
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                error = result.exception.message ?: "Error desconocido",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al procesar imágenes: ${e.message}", isLoading = false) }
            }
        }
    }

    private fun base64ToFile(base64String: String, fileName: String): File {
        val file = File(context.cacheDir, fileName)

        val pureBase64 = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }

        val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
        file.writeBytes(bytes)
        return file
    }
}
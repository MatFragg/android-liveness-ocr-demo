package com.matfragg.rekognition_demo.presentation.document_ocr.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DniResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DniResultState())
    val state: StateFlow<DniResultState> = _state.asStateFlow()

    // El DNI result se pasará desde DocumentScanViewModel
    fun setDniData(dniData: com.matfragg.rekognition_demo.domain.document_ocr.model.DniData) {
        _state.value = _state.value.copy(dniData = dniData)
    }
}
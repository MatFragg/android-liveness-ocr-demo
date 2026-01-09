package com.matfragg.rekognition_demo.presentation.document_ocr.result

import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData

data class DniResultState(
    val dniData: DniData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
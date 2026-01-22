package com.matfragg.rekognition_demo.presentation.document_ocr.scan

import com.matfragg.rekognition_demo.domain.document_ocr.model.DocumentType

data class DocumentScanState(
    val isLoading: Boolean = false,
    val currentStep: ScanStep = ScanStep.FRONT,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val error: String? = null,
    val documentType: DocumentType = DocumentType.DNI_FRONT,
    val isAligned: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val permissionShowRationale: Boolean = false
)

enum class ScanStep {
    FRONT, BACK, PREVIEW, PROCESSING, COMPLETE
}
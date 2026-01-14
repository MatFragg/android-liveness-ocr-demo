package com.matfragg.rekognition_demo.presentation.onboarding

import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceComparison
import com.matfragg.rekognition_demo.domain.reniec.model.ReniecValidation

data class OnboardingState(
    val dniData: DniData? = null,
    val livenessPhotoBase64: String? = null,
    val comparisonResult: FaceComparison? = null,
    val reniecResult: ReniecValidation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
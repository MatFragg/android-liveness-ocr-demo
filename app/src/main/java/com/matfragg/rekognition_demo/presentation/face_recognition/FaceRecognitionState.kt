package com.matfragg.rekognition_demo.presentation.face_recognition

data class FaceRecognitionState(
    val isComparisonMode: Boolean = true,
    val statusMessage: String = "Listo para comparar rostros",
    val photo1: java.io.File? = null,
    val photo2: java.io.File? = null
)
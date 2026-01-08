package com.matfragg.rekognition_demo.domain.face_recognition.model

data class FaceComparison(
    val similarity: Double,
    val quality: ComparisonQuality,
    val status: ComparisonStatus
) {
    fun isMatch(threshold: Double = 90.0): Boolean = similarity >= threshold
}

data class ComparisonQuality(
    val sourceQuality: Double,
    val targetQuality: Double
)

enum class ComparisonStatus {
    SUCCESS,
    NO_FACE_DETECTED,
    MULTIPLE_FACES,
    POOR_QUALITY,
    ERROR
}
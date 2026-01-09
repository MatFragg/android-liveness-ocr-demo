package com.matfragg.rekognition_demo.domain.document_ocr.model

data class DocumentQuality(
    val brightness: Double,
    val sharpness: Double,
    val glare: Double,
    val overallScore: Double
) {
    fun isAcceptable(): Boolean = overallScore >= 0.7

    fun getQualityLevel(): QualityLevel {
        return when {
            overallScore >= 0.9 -> QualityLevel.EXCELLENT
            overallScore >= 0.7 -> QualityLevel.GOOD
            overallScore >= 0.5 -> QualityLevel.FAIR
            else -> QualityLevel.POOR
        }
    }
}

enum class QualityLevel {
    EXCELLENT, GOOD, FAIR, POOR
}

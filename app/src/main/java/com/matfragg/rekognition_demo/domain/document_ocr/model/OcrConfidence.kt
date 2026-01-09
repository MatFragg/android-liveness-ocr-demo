package com.matfragg.rekognition_demo.domain.document_ocr.model

data class OcrConfidence(
    val textDetection: Double,
    val fieldExtraction: Double,
    val overall: Double
) {
    fun isReliable(): Boolean = overall >= 0.8

    fun getConfidenceLevel(): ConfidenceLevel {
        return when {
            overall >= 0.9 -> ConfidenceLevel.HIGH
            overall >= 0.7 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }
}

enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW
}

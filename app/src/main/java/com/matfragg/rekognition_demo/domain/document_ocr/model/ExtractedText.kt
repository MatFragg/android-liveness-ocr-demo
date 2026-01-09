package com.matfragg.rekognition_demo.domain.document_ocr.model

data class ExtractedText(
    val fullText: String,
    val confidence: Double,
    val detectedLanguage: String? = null,
    val words: List<DetectedWord> = emptyList()
)

data class DetectedWord(
    val text: String,
    val confidence: Double,
    val boundingBox: BoundingBox? = null
)

data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
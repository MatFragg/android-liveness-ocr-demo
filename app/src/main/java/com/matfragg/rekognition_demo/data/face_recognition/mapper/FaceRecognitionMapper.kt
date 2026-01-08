package com.matfragg.rekognition_demo.data.face_recognition.mapper

import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceComparisonDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionDto
import com.matfragg.rekognition_demo.domain.face_recognition.model.*

class FaceRecognitionMapper {

    fun toDomain(dto: FaceDetectionDto): FaceAnalysis {
        return FaceAnalysis(
            confidence = dto.confidence.toDoubleOrNull() ?: 0.0,
            quality = FaceQuality(
                brightness = 0.0, // Extraer de quality string
                sharpness = 0.0
            ),
            isAlive = dto.alive.equals("true", ignoreCase = true),
            emotions = dto.emotions?.let { parseEmotions(it) }
        )
    }

    fun toDomain(dto: FaceComparisonDto): FaceComparison {
        return FaceComparison(
            similarity = dto.similarity.toDoubleOrNull() ?: 0.0,
            quality = ComparisonQuality(
                sourceQuality = 0.0,
                targetQuality = 0.0
            ),
            status = when (dto.status.lowercase()) {
                "success" -> ComparisonStatus.SUCCESS
                else -> ComparisonStatus.ERROR
            }
        )
    }

    private fun parseEmotions(emotionsString: String): List<Emotion> {
        return emotionsString.split(",")
            .mapNotNull { emotion ->
                try {
                    Emotion.valueOf(emotion.trim().uppercase())
                } catch (e: Exception) {
                    null
                }
            }
    }
}
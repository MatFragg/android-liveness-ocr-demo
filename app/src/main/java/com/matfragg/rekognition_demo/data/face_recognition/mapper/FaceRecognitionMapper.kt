package com.matfragg.rekognition_demo.data.face_recognition.mapper

import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceComparisonDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionResponseDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.NewCompareResponseDto
import com.matfragg.rekognition_demo.domain.face_recognition.model.*

class FaceRecognitionMapper {

    fun toDomain(response: FaceDetectionResponseDto): FaceAnalysis {
        val dto = response.faces?.firstOrNull()

        return FaceAnalysis(
            confidence = dto?.confidence ?: 0.0,
            quality = FaceQuality(brightness = 0.0, sharpness = 0.0),
            // En Java usas "hasEyesOpen" dentro de "attributes"
            isAlive = dto?.attributes?.get("hasEyesOpen") ?: false,
            emotions = dto?.emotions?.map { emotionDto ->
                try {
                    Emotion.valueOf(emotionDto.type?.uppercase() ?: "CALM")
                } catch (e: Exception) {
                    Emotion.CALM
                }
            } ?: emptyList()
        )
    }

    fun toDomain(dto: FaceComparisonDto): FaceComparison {
        return FaceComparison(
            similarity = dto.similarityScore ?: 0.0,
            quality = ComparisonQuality(
                sourceQuality = 0.0,
                targetQuality = 0.0
            ),
            status = when (dto.status?.lowercase()) {
                "success" -> ComparisonStatus.SUCCESS
                else -> ComparisonStatus.ERROR
            }
        )
    }

    fun toDomain(dto: NewCompareResponseDto): FaceComparison {
        // Extraemos la data y el resultado de la estructura anidada
        val comparisonData = dto.data
        val resultInfo = dto.result

        return FaceComparison(
            // El API devuelve similarityScore como Double
            similarity = comparisonData?.similarityScore ?: 0.0,
            quality = ComparisonQuality(
                sourceQuality = 0.0,
                targetQuality = 0.0
            ),
            // Mapeamos el status basado en el código "000" o el booleano match
            status = if (resultInfo?.code == "000" && comparisonData?.match == true) {
                ComparisonStatus.SUCCESS
            } else {
                ComparisonStatus.ERROR
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
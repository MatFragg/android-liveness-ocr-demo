package com.matfragg.rekognition_demo.data.face_recognition.remote.dto

import com.google.gson.annotations.SerializedName

data class FaceDetectionResponseDto(
    @SerializedName("faceCount") val faceCount: Int?,
    @SerializedName("faces") val faces: List<FaceDetectionDto>?
)

data class FaceDetectionDto(
    @SerializedName("confidence") val confidence: Double?,
    @SerializedName("emotions") val emotions: List<EmotionDto>?,
    @SerializedName("gender") val gender: GenderDto?,
    @SerializedName("attributes") val attributes: Map<String, Boolean>?
)

data class EmotionDto(
    @SerializedName("type") val type: String?,
    @SerializedName("confidence") val confidence: Double?
)

data class GenderDto(
    @SerializedName("value") val value: String?
)
data class FaceComparisonDto(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("similarityScore") val similarityScore: Double?,
    @SerializedName("isMatch") val isMatch: Boolean?,
    @SerializedName("confidenceLevel") val confidenceLevel: String?
)
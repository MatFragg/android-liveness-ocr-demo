package com.matfragg.rekognition_demo.data.face_recognition.remote.dto

import com.google.gson.annotations.SerializedName

data class NewCompareRequest(
    val imageFirst: String, // Base64
    val imageSecond: String // Base64
)

// Respuesta de Comparación
data class NewCompareResponseDto(
    @SerializedName("result") val result: ResultDto?,
    @SerializedName("data") val data: ComparisonDataDto?
)

data class ResultDto(val code: String, val info: String)
data class ComparisonDataDto(val similarityScore: Double, val match: Boolean)
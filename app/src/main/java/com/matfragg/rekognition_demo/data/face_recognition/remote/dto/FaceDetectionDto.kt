package com.matfragg.rekognition_demo.data.face_recognition.remote.dto

import com.google.gson.annotations.SerializedName

data class FaceDetectionDto(
    @SerializedName("mode") val mode: String,
    @SerializedName("confidence") val confidence: String,
    @SerializedName("quality") val quality: String,
    @SerializedName("alive") val alive: String,
    @SerializedName("emotions") val emotions: String?
)

data class FaceComparisonDto(
    @SerializedName("mode") val mode: String,
    @SerializedName("similarity") val similarity: String,
    @SerializedName("quality") val quality: String,
    @SerializedName("status") val status: String
)
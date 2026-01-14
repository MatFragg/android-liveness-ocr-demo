package com.matfragg.rekognition_demo.data.liveness.remote.dto

import com.google.gson.annotations.SerializedName

data class LivenessSessionDto(
    @SerializedName("sessionId") val sessionId: String
)

data class LivenessResultDto(
    @SerializedName("isLive") val isLive: Boolean,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("status") val status: String,
    @SerializedName("ReferenceImage") val referenceImage: ReferenceImageDto?
)

data class ReferenceImageDto(
    @SerializedName("Bytes") val bytes: String?
)
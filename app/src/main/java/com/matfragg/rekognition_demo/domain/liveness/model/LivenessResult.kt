package com.matfragg.rekognition_demo.domain.liveness.model

data class LivenessResult(
    val isLive: Boolean,
    val confidence: Double,
    val status: LivenessStatus,
    val fotoBase64: String? = null
)

enum class LivenessStatus {
    SUCCEEDED,
    FAILED,
    ERROR
}

package com.matfragg.rekognition_demo.domain.liveness.model

data class LivenessResult(
    val isLive: Boolean,
    val confidence: Double,
    val status: LivenessStatus
)

enum class LivenessStatus {
    SUCCEEDED,
    FAILED,
    ERROR
}

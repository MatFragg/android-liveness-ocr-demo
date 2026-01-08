package com.matfragg.rekognition_demo.domain.liveness.model

data class LivenessSession(
    val sessionId: String,
    val region: String = "us-east-1",
    val cameraMode: CameraMode = CameraMode.FRONT
)

enum class CameraMode {
    FRONT, BACK
}
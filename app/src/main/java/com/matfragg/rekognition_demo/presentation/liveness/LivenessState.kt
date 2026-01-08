package com.matfragg.rekognition_demo.presentation.liveness

import com.matfragg.rekognition_demo.domain.liveness.model.CameraMode
import com.matfragg.rekognition_demo.domain.liveness.model.LivenessResult

data class LivenessState(
    val sessionId: String? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val currentCamera: CameraMode = CameraMode.FRONT,
    val showCameraSwitch: Boolean = true,
    val result: LivenessResult? = null
)
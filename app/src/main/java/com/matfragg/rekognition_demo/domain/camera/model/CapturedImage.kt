package com.matfragg.rekognition_demo.domain.camera.model

import java.io.File

data class CapturedImage(
    val file: File,
    val timestamp: Long = System.currentTimeMillis()
)
package com.matfragg.rekognition_demo.domain.face_recognition.model

data class FaceAnalysis(
    val confidence: Double,
    val quality: FaceQuality,
    val isAlive: Boolean,
    val emotions: List<Emotion>? = null
)

data class FaceQuality(
    val brightness: Double,
    val sharpness: Double
)

enum class Emotion(val displayName: String) {
    HAPPY("Feliz"),
    SAD("Triste"),
    ANGRY("Enojado"),
    CONFUSED("Confundido"),
    DISGUSTED("Disgustado"),
    SURPRISED("Sorprendido"),
    CALM("Calmado"),
    FEAR("Miedo")
}
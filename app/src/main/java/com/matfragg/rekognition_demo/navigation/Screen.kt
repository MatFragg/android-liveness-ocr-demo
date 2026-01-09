package com.matfragg.rekognition_demo.navigation

sealed class Screen(val route: String) {
    // Pantalla Principal (Menú)
    object Main : Screen("main")

    // Face Recognition (antes era Main)
    object FaceRecognition : Screen("face_recognition")

    // Liveness
    object Liveness : Screen("liveness")
    object LivenessResult : Screen("liveness_result/{sessionId}") {
        fun createRoute(sessionId: String) = "liveness_result/$sessionId"
    }

    // Document OCR
    object DocumentScan : Screen("document/scan")
    object DniResult : Screen("document/dni/result")
}
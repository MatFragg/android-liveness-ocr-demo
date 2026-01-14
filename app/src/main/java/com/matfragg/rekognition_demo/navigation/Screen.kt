package com.matfragg.rekognition_demo.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object FaceRecognition : Screen("face_recognition")
    object Liveness : Screen("liveness")

    // Rutas de Onboarding
    object Onboarding : Screen("onboarding")
    object OnboardingStart : Screen("start")
    object DocumentScan : Screen("ocr")
    object Choice : Screen("choice")
    object ComparisonResult : Screen("result")
    object ReniecResult : Screen("reniec_result")
}
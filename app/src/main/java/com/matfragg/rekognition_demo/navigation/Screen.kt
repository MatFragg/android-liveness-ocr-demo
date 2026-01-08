package com.matfragg.rekognition_demo.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Liveness : Screen("liveness")
    object LivenessResult : Screen("liveness_result/{sessionId}") {
        fun createRoute(sessionId: String) = "liveness_result/$sessionId"
    }
    object DocumentScan : Screen("document/scan")
    object DniResult : Screen("document/dni/result")
}
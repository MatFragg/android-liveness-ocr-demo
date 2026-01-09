package com.matfragg.rekognition_demo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matfragg.rekognition_demo.presentation.document_ocr.result.DniResultScreen
import com.matfragg.rekognition_demo.presentation.document_ocr.scan.DocumentScanScreen
import com.matfragg.rekognition_demo.presentation.document_ocr.scan.DocumentScanViewModel
import com.matfragg.rekognition_demo.presentation.face_recognition.FaceRecognitionScreen
import com.matfragg.rekognition_demo.presentation.liveness.LivenessScreen
import com.matfragg.rekognition_demo.presentation.main.MainScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        // PANTALLA PRINCIPAL - MENÚ DE NAVEGACIÓN
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToFaceRecognition = {
                    navController.navigate(Screen.FaceRecognition.route)
                },
                onNavigateToLiveness = {
                    navController.navigate(Screen.Liveness.route)
                },
                onNavigateToDocumentScan = {
                    navController.navigate(Screen.DocumentScan.route)
                }
            )
        }

        // FACE RECOGNITION (la anterior MainScreen)
        composable(Screen.FaceRecognition.route) {
            FaceRecognitionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // LIVENESS
        composable(Screen.Liveness.route) {
            LivenessScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onComplete = { sessionId ->
                    // Handle liveness complete
                    navController.popBackStack()
                }
            )
        }

        // DOCUMENT SCAN FLOW
        composable(Screen.DocumentScan.route) {
            val viewModel: DocumentScanViewModel = hiltViewModel()
            val dniResult by viewModel.dniResult.collectAsState()

            DocumentScanScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onScanComplete = { dniNumber ->
                    // Navegar a resultados con el DNI data
                    dniResult?.let { dni ->
                        navController.navigate(Screen.DniResult.route)
                    }
                }
            )
        }

        composable(Screen.DniResult.route) {
            val previousEntry = remember(navController.currentBackStackEntry) {
                navController.previousBackStackEntry
            }
            val viewModel: DocumentScanViewModel? = previousEntry?.let { hiltViewModel(it) }
            val dniData by (viewModel?.dniResult?.collectAsState() ?: return@composable)

            dniData?.let { dni ->
                DniResultScreen(
                    dniData = dni,
                    onNavigateBack = {
                        navController.popBackStack(Screen.Main.route, inclusive = false)
                    },
                    onSave = { data ->
                        // TODO: Implementar guardado
                        navController.popBackStack(Screen.Main.route, inclusive = false)
                    }
                )
            }
        }
    }
}
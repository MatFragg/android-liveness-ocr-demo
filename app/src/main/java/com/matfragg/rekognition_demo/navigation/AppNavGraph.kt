package com.matfragg.rekognition_demo.navigation
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.matfragg.rekognition_demo.presentation.document_ocr.result.DniResultScreen
import com.matfragg.rekognition_demo.presentation.document_ocr.scan.DocumentScanScreen
import com.matfragg.rekognition_demo.presentation.document_ocr.scan.DocumentScanViewModel
import com.matfragg.rekognition_demo.presentation.face_recognition.FaceRecognitionScreen
import com.matfragg.rekognition_demo.presentation.liveness.LivenessScreen
import com.matfragg.rekognition_demo.presentation.liveness.LivenessViewModel
import com.matfragg.rekognition_demo.presentation.main.MainScreen
import com.matfragg.rekognition_demo.presentation.onboarding.ChoiceScreen
import com.matfragg.rekognition_demo.presentation.onboarding.FinalResultScreen
import com.matfragg.rekognition_demo.presentation.onboarding.OnboardingStartScreen
import com.matfragg.rekognition_demo.presentation.onboarding.OnboardingViewModel
import com.matfragg.rekognition_demo.presentation.reniec.ReniecResultScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // La aplicación ahora solo conoce el flujo de onboarding
    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        onboardingGraph(navController)
    }
}

fun NavGraphBuilder.onboardingGraph(navController: NavController) {
    navigation(startDestination = "start", route = "onboarding") {

        composable("start") {
            OnboardingStartScreen { navController.navigate("ocr") }
        }

        composable("ocr") { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry("onboarding") }
            val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            val scanViewModel: DocumentScanViewModel = hiltViewModel()
            val dniResultState by scanViewModel.dniResult.collectAsState()

            DocumentScanScreen(
                viewModel = scanViewModel,
                onNavigateBack = { navController.popBackStack() },
                onScanComplete = { _ ->
                    dniResultState?.let { fullDniData ->
                        sharedViewModel.onDniCaptured(fullDniData)
                        navController.navigate("liveness")
                    }
                }
            )
        }

        composable("liveness") { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry("onboarding") }
            val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            val livenessViewModel: LivenessViewModel = hiltViewModel()
            val livenessState by livenessViewModel.state.collectAsState()

            LivenessScreen(
                viewModel = livenessViewModel,
                onNavigateBack = { navController.popBackStack() },
                onComplete = { sessionId ->
                    livenessViewModel.onLivenessComplete(sessionId)
                },
                onContinue = {
                    val result = livenessState.result
                    if (result != null && result.isLive) {
                        sharedViewModel.onLivenessCompleted(result.fotoBase64 ?: "")

                        navController.navigate("choice") {
                            popUpTo("liveness") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("choice") { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry("onboarding") }
            val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            val state by sharedViewModel.state.collectAsState()

            ChoiceScreen(
                state = state,
                onCompareClick = {
                    sharedViewModel.compareFacial()
                    navController.navigate("result")
                },
                onReniecClick = {
                    // 🚀 ACTIVAMOS LA VALIDACIÓN REAL CON TU BACKEND
                    sharedViewModel.validateWithReniec()
                    navController.navigate("reniec_result")
                }
            )
        }

        composable("result") { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry("onboarding") }
            val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            val state by sharedViewModel.state.collectAsState()

            FinalResultScreen(state = state)
        }

        composable("reniec_result") { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry("onboarding") }
            val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            val state by sharedViewModel.state.collectAsState()

            ReniecResultScreen(state = state)
        }
    }
}
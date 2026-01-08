package com.matfragg.rekognition_demo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToLiveness = {
                    navController.navigate(Screen.Liveness.route)
                }
            )
        }

        composable(Screen.Liveness.route) {
            LivenessScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onComplete = { sessionId ->
                    navController.navigate(Screen.LivenessResult.createRoute(sessionId))
                }
            )
        }

        composable(Screen.Liveness.route) {
            LivenessScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { sessionId ->
                    // Volvemos atrás y pasamos el resultado (o navegamos a una nueva)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("completed_session_id", sessionId)
                    navController.popBackStack()
                }
            )
        }
    }
}
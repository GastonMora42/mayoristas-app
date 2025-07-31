package com.mayoristas.feature.auth.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mayoristas.feature.auth.presentation.screens.LoginScreen
import com.mayoristas.feature.auth.presentation.screens.RegisterScreen

// Navigation Routes
object AuthRoute {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AuthNavigation(
    navController: NavHostController = rememberNavController(),
    onNavigateToHome: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoute.LOGIN
    ) {
        authNavGraph(
            navController = navController,
            onNavigateToHome = onNavigateToHome
        )
    }
}

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onNavigateToHome: () -> Unit
) {
    composable(AuthRoute.LOGIN) {
        LoginScreen(
            onNavigateToRegister = {
                navController.navigate(AuthRoute.REGISTER) {
                    // Evitar múltiples copias del registro en el stack
                    launchSingleTop = true
                }
            },
            onNavigateToHome = onNavigateToHome
        )
    }
    
    composable(AuthRoute.REGISTER) {
        RegisterScreen(
            onNavigateToLogin = {
                navController.navigate(AuthRoute.LOGIN) {
                    // Clear register screen from stack
                    popUpTo(AuthRoute.LOGIN) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            onNavigateToHome = onNavigateToHome
        )
    }
}
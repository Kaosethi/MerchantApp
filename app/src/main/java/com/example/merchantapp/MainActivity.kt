package com.example.merchantapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.merchantapp.navigation.AppDestinations
import com.example.merchantapp.ui.login.LoginScreen
import com.example.merchantapp.ui.main.MainScreen
import com.example.merchantapp.ui.qr.QrScanScreen
import com.example.merchantapp.ui.register.RegisterScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        enableEdgeToEdge()
        setContent {
            MerchantAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppDestinations.REGISTER_ROUTE) },
                onLoginSuccess = {
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.MAIN_ROUTE) {
            val onNavigateToQrScan: (String) -> Unit = { amount ->
                Log.d("AppNavigation", "Navigating to QR Scan with amount: $amount")
                navController.navigate(AppDestinations.createQrScanRoute(amount))
            }

            MainScreen(
                onLogoutRequest = {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToQrScan = onNavigateToQrScan
            )
        }

        composable(
            route = AppDestinations.QR_SCAN_ROUTE, // Use route with placeholder
            arguments = listOf(navArgument("amount") { type = NavType.StringType }) // Define argument
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.00" // Extract argument

            QrScanScreen(
                amount = amount,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
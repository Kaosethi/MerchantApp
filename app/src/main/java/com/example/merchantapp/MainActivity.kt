// File: app/src/main/java/com/example/merchantapp/MainActivity.kt
package com.example.merchantapp

// --- Imports ---
// Use Optimize Imports after pasting
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
// REMOVED: androidx.compose.material3.Text import if only used for testing
import androidx.compose.runtime.Composable
// REMOVED: androidx.compose.runtime.remember import if not used here
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.merchantapp.navigation.AppDestinations
import com.example.merchantapp.ui.confirmation.TransactionConfirmationScreen
import com.example.merchantapp.ui.login.LoginScreen
import com.example.merchantapp.ui.main.MainScreen
import com.example.merchantapp.ui.qr.QrScanScreen
import com.example.merchantapp.ui.register.RegisterScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme
// REMOVED: URLDecoder, StandardCharsets, PinEntryScreen imports
// --- End Imports ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Call super first
        Log.d("MainActivity", "onCreate called")

        enableEdgeToEdge()
        setContent {
            MerchantAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Call the main navigation composable
                }
            }
        }
        Log.d("MainActivity", "onCreate finished")
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val hardcodedTestToken = "TEST-TOKEN-12345"

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE
    ) {
        // Login Screen
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

        // Register Screen
        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Main Screen (Calling the actual MainScreen)
        composable(AppDestinations.MAIN_ROUTE) {
            val onNavigateToQrScan: (String) -> Unit = { amount ->
                Log.d("AppNavigation", "Navigating to QR Scan with amount: $amount")
                navController.navigate(AppDestinations.createQrScanRoute(amount))
            }
            MainScreen( // Call the real MainScreen
                onLogoutRequest = {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToQrScan = onNavigateToQrScan
            )
            // REMOVED: Test Area Text
        }

        // QR Scan Screen
        composable(
            route = AppDestinations.QR_SCAN_ROUTE,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.00"
            val onSimulatedScan: (String, String) -> Unit = { scannedAmount, scannedToken ->
                Log.d("AppNavigation", "Simulated scan success. Token: $scannedToken, Amount: $scannedAmount")
                if (scannedToken == hardcodedTestToken) {
                    val placeholderBeneficiaryId = "BEN-SIM-001"
                    val placeholderBeneficiaryName = "Simulated User" // Raw name
                    Log.d("AppNavigation", "Simulated token verified. Navigating to Confirmation.")
                    // Use reverted route creation (raw strings)
                    val route = AppDestinations.createTransactionConfirmationRoute(
                        amount = scannedAmount,
                        beneficiaryId = placeholderBeneficiaryId,
                        beneficiaryName = placeholderBeneficiaryName
                    )
                    navController.navigate(route){
                        popUpTo(AppDestinations.QR_SCAN_ROUTE) { inclusive = true }
                    }
                } else { /* ... error handling ... */ }
            }
            QrScanScreen(
                amount = amount,
                onNavigateBack = { navController.popBackStack() },
                onSimulatedScan = onSimulatedScan
            )
        }

        // Transaction Confirmation Screen
        composable(
            route = AppDestinations.TRANSACTION_CONFIRMATION_ROUTE,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType } // Expects raw name
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "Error"
            val beneficiaryId = backStackEntry.arguments?.getString("beneficiaryId") ?: "Error"
            val beneficiaryName = backStackEntry.arguments?.getString("beneficiaryName") ?: "Error" // Get raw name

            TransactionConfirmationScreen(
                amount = amount,
                beneficiaryId = beneficiaryId,
                beneficiaryName = beneficiaryName, // Pass raw name
                onNavigateBack = { navController.popBackStack() }
                // REMOVED: onNavigateToPinEntry lambda
            )
        }

        // REMOVED: Pin Entry Screen composable block
    }
}

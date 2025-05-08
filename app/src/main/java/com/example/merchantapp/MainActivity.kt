// File: app/src/main/java/com/example/merchantapp/MainActivity.kt
package com.example.merchantapp

// --- Imports ---
// import androidx.compose.material3.Text // REMOVED: No longer needed for TransactionSuccessScreen placeholder
// ADDED: Import for TransactionSuccessScreen
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.merchantapp.navigation.AppDestinations
import com.example.merchantapp.ui.confirmation.TransactionConfirmationScreen
import com.example.merchantapp.ui.forgotpassword.ForgotPasswordScreen
import com.example.merchantapp.ui.login.LoginScreen
import com.example.merchantapp.ui.main.MainScreen
import com.example.merchantapp.ui.otp.OtpEntryScreen
import com.example.merchantapp.ui.pinentry.PinEntryScreen
import com.example.merchantapp.ui.qr.QrScanScreen
import com.example.merchantapp.ui.register.RegisterScreen
import com.example.merchantapp.ui.setnewpassword.SetNewPasswordScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.ui.transactionsuccess.TransactionSuccessScreen

// --- End Imports ---

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
    val context = LocalContext.current // Get context for Toast
    val hardcodedTestToken = "TEST-TOKEN-12345" // Keep if used elsewhere

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
                },
                onNavigateToForgotPassword = {
                    Log.d("AppNavigation", "Navigating to Forgot Password (Enter Email Screen)")
                    navController.navigate(AppDestinations.FORGOT_PASSWORD_ROUTE)
                }
            )
        }

        // Register Screen
        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Forgot Password Screen (Enter Email)
        composable(AppDestinations.FORGOT_PASSWORD_ROUTE) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onOtpSentNavigateToOtpEntry = { email ->
                    Log.d("AppNavigation", "OTP 'sent' to $email. Navigating to OTP Entry Screen.")
                    Toast.makeText(context, "OTP 'sent' to $email (Mocked). Use 111111.", Toast.LENGTH_LONG).show()
                    navController.navigate(AppDestinations.createOtpEntryRoute(email)) {
                        popUpTo(AppDestinations.FORGOT_PASSWORD_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // OTP Entry Screen
        composable(
            route = AppDestinations.OTP_ENTRY_ROUTE_PATTERN,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            OtpEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onOtpVerifiedNavigateToSetPassword = { verifiedEmail ->
                    Log.d("AppNavigation", "OTP Verified for $verifiedEmail. Navigating to Set New Password.")
                    Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppDestinations.createSetNewPasswordRoute(verifiedEmail)) {
                        val otpRouteWithArg = AppDestinations.OTP_ENTRY_ROUTE_PATTERN.replace("{email}", verifiedEmail)
                        popUpTo(otpRouteWithArg) { inclusive = true }
                    }
                },
                onRequestResendOtp = {
                    Log.d("AppNavigation", "onRequestResendOtp callback triggered in NavHost (VM handles actual logic).")
                }
            )
        }

        // Set New Password Screen
        composable(
            route = AppDestinations.SET_NEW_PASSWORD_ROUTE_PATTERN,
            arguments = listOf(navArgument("email_or_token") { type = NavType.StringType })
        ) {
            SetNewPasswordScreen(
                onPasswordSetNavigateToLogin = {
                    Log.d("AppNavigation", "Password successfully reset. Navigating to Login.")
                    Toast.makeText(context, "Password reset successfully! Please log in.", Toast.LENGTH_LONG).show()
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }


        // Main Screen
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
                    val placeholderBeneficiaryName = "Simulated User"
                    Log.d("AppNavigation", "Simulated token verified. Navigating to Confirmation.")
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
                navArgument("beneficiaryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "Error"
            val beneficiaryId = backStackEntry.arguments?.getString("beneficiaryId") ?: "Error"
            val beneficiaryName = backStackEntry.arguments?.getString("beneficiaryName") ?: "Error"

            TransactionConfirmationScreen(
                amount = amount,
                beneficiaryId = beneficiaryId,
                beneficiaryName = beneficiaryName,
                onNavigateBack = { navController.popBackStack() },
                onConfirmAndProcess = { navAmount, navBeneficiaryId, navBeneficiaryName, navCategory ->
                    Log.d("AppNavigation", "Navigating to PIN Entry. Amount: $navAmount, Category: $navCategory")
                    val pinEntryRoute = AppDestinations.createPinEntryRoute(
                        amount = navAmount,
                        beneficiaryId = navBeneficiaryId,
                        beneficiaryName = navBeneficiaryName,
                        category = navCategory
                    )
                    navController.navigate(pinEntryRoute)
                    // No popUpTo here, so user can go back from PIN to Confirmation if they want.
                }
            )
        }

        // PIN Entry Screen
        composable(
            route = AppDestinations.PIN_ENTRY_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) {
            PinEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onPinVerifiedNavigateToSuccess = { navAmount, navBeneficiaryId, navBeneficiaryName, navCategory ->
                    Log.d("AppNavigation", "PIN Verified. Navigating to Transaction Success. Amount: $navAmount, Category: $navCategory")
                    Toast.makeText(context, "PIN Verified!", Toast.LENGTH_SHORT).show()
                    val mockTransactionId = "TXN-${System.currentTimeMillis()}"
                    val successRoute = AppDestinations.createTransactionSuccessRoute(
                        amount = navAmount,
                        beneficiaryId = navBeneficiaryId,
                        beneficiaryName = navBeneficiaryName,
                        category = navCategory,
                        transactionId = mockTransactionId
                    )
                    navController.navigate(successRoute) {
                        // Pop PIN entry screen off the stack
                        val pinEntryRouteWithArgs = AppDestinations.PIN_ENTRY_ROUTE_PATTERN
                            .replace("{amount}", navAmount)
                            .replace("{beneficiaryId}", navBeneficiaryId)
                            .replace("{beneficiaryName}", navBeneficiaryName)
                            .replace("{category}", navCategory)
                        popUpTo(pinEntryRouteWithArgs) { inclusive = true }

                        // MODIFIED: Also pop TransactionConfirmationScreen to prevent going back to it
                        // from the success screen.
                        val confirmRoute = AppDestinations.TRANSACTION_CONFIRMATION_ROUTE
                            .replace("{amount}", navAmount) // Use the current amount for the route pattern
                            .replace("{beneficiaryId}", navBeneficiaryId) // Use the current ID
                            .replace("{beneficiaryName}", navBeneficiaryName) // Use the current name
                        popUpTo(confirmRoute) { inclusive = true }
                    }
                }
            )
        }

        // MODIFIED: Transaction Success Screen - Now calls the actual screen
        composable(
            route = AppDestinations.TRANSACTION_SUCCESS_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType },
                navArgument("transactionId") {type = NavType.StringType }
            )
        ) {
            // TransactionSuccessViewModel will handle retrieving arguments via SavedStateHandle
            TransactionSuccessScreen(
                onNavigateToHome = {
                    Log.d("AppNavigation", "Transaction Success: Navigating to Main Screen.")
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        // Pop everything up to the Main screen (inclusive if it's already on top,
                        // or just pop all screens above a new Main screen instance).
                        // This ensures a clean stack back to the home/main area.
                        popUpTo(AppDestinations.MAIN_ROUTE) {
                            inclusive = false // Keep the MAIN_ROUTE itself if navigating to it fresh
                            // Set to true if MAIN_ROUTE could be an intermediate screen you want to clear.
                            // For returning to a primary home screen, 'false' is often desired.
                        }
                        launchSingleTop = true // Ensure only one instance of MainScreen
                    }
                }
            )
        }
    }
}
// File: app/src/main/java/com/example/merchantapp/MainActivity.kt
// MODIFIED: Implemented startDestination logic in AppNavigation based on login status.
package com.example.merchantapp

import android.content.Context
import android.os.Bundle
import android.util.Log // ADDED: Import Log if not already present
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // ADDED: For observing ViewModel state
import androidx.compose.runtime.collectAsState // ADDED: For collecting state flow
import androidx.compose.runtime.getValue // ADDED: For collecting state flow
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel // ADDED: For getting ViewModel instance
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.merchantapp.data.local.AuthManager // Ensure AuthManager is correctly imported
import com.example.merchantapp.navigation.AppDestinations // Ensure AppDestinations is correctly imported
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
import com.example.merchantapp.viewmodel.QrScanViewModel // ADDED: Import QrScanViewModel

// --- MainActivity class remains the same ---
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
                    // Pass the Application Context, needed by AuthManager
                    AppNavigation(applicationContext = this.applicationContext)
                }
            }
        }
        Log.d("MainActivity", "onCreate finished")
    }
}
// --- AppNavigation ---
@Composable
fun AppNavigation(applicationContext: Context) {
    val navController = rememberNavController()
    // LocalContext provides the Context of the current Composable, useful for Toasts etc.
    val composableContext = LocalContext.current

    // --- FIXED: Implement the start destination logic ---
    // Determine the starting screen based on login status
    val startDestination = remember {
        // Use your AuthManager (or however you check login status)
        // Make sure AuthManager.isLoggedIn exists and works correctly
        if (AuthManager.isLoggedIn(applicationContext)) {
            Log.d("AppNavigation", "User is logged in, starting at Main.")
            AppDestinations.MAIN_ROUTE // Return MAIN_ROUTE string if logged in
        } else {
            Log.d("AppNavigation", "User is NOT logged in, starting at Login.")
            AppDestinations.LOGIN_ROUTE // Return LOGIN_ROUTE string if not logged in
        }
    }
    // --- End FIXED section ---


    NavHost(
        navController = navController,
        // Pass the calculated startDestination String directly
        startDestination = startDestination // REMOVED .toString()
    ) {
        // --- Login Screen ---
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
                    Log.d("AppNavigation", "Navigating to Forgot Password")
                    navController.navigate(AppDestinations.FORGOT_PASSWORD_ROUTE)
                }
            )
        }

        // --- Register Screen ---
        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Forgot Password Screen ---
        composable(AppDestinations.FORGOT_PASSWORD_ROUTE) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onOtpSentNavigateToOtpEntry = { email ->
                    Log.d("AppNavigation", "Navigating to OTP Entry for $email")
                    // Use composableContext for Toast inside composable
                    Toast.makeText(composableContext, "OTP 'sent' (Mocked: 111111)", Toast.LENGTH_LONG).show()
                    navController.navigate(AppDestinations.createOtpEntryRoute(email)) {
                        popUpTo(AppDestinations.FORGOT_PASSWORD_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // --- OTP Entry Screen ---
        composable(
            route = AppDestinations.OTP_ENTRY_ROUTE_PATTERN,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            OtpEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onOtpVerifiedNavigateToSetPassword = { verifiedEmail ->
                    Log.d("AppNavigation", "Navigating to Set New Password for $verifiedEmail")
                    Toast.makeText(composableContext, "OTP Verified!", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppDestinations.createSetNewPasswordRoute(verifiedEmail)) {
                        val otpRouteWithArg = AppDestinations.OTP_ENTRY_ROUTE_PATTERN.replace("{email}", verifiedEmail)
                        popUpTo(otpRouteWithArg) { inclusive = true }
                    }
                },
                onRequestResendOtp = { Log.d("AppNavigation", "Resend OTP requested") }
            )
        }

        // --- Set New Password Screen ---
        composable(
            route = AppDestinations.SET_NEW_PASSWORD_ROUTE_PATTERN,
            arguments = listOf(navArgument("email_or_token") { type = NavType.StringType })
        ) {
            SetNewPasswordScreen(
                onPasswordSetNavigateToLogin = {
                    Log.d("AppNavigation", "Navigating to Login after password reset")
                    Toast.makeText(composableContext, "Password reset successfully!", Toast.LENGTH_LONG).show()
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // --- Main Screen ---
        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen(
                onLogoutRequest = {
                    // Use applicationContext for AuthManager operations
                    AuthManager.clearLoginData(applicationContext)
                    Log.d("AppNavigation", "User logged out. Navigating to Login.")
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToQrScan = { amount ->
                    Log.d("AppNavigation", "Navigating to QR Scan with amount: $amount")
                    navController.navigate(AppDestinations.createQrScanRoute(amount))
                }
            )
        }


        // --- QR Scan Screen ---
        composable(
            route = AppDestinations.QR_SCAN_ROUTE, // Route still has {amount} argument
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry -> // Get backStackEntry to retrieve arguments if needed, VM also does this
            // Get ViewModel instance scoped to this NavGraph entry
            val qrViewModel: QrScanViewModel = viewModel()
            val uiState by qrViewModel.uiState.collectAsState()

            // Observe the validation success state from the ViewModel
            LaunchedEffect(uiState.validationSuccess, uiState.validatedBeneficiary) {
                if (uiState.validationSuccess && uiState.validatedBeneficiary != null) {
                    val beneficiary = uiState.validatedBeneficiary!! // Safe non-null access here
                    Log.d("AppNavigation", "QR Validation Success in NavHost. Navigating to Confirmation. Beneficiary ID: ${beneficiary.id}, Name: ${beneficiary.name}, Amount: ${uiState.amount}")

                    val route = AppDestinations.createTransactionConfirmationRoute(
                        amount = uiState.amount, // Use amount from ViewModel state
                        beneficiaryId = beneficiary.id, // Use validated ID
                        beneficiaryName = beneficiary.name // Use validated Name
                    )
                    navController.navigate(route) {
                        // Pop QrScanScreen off the stack after successful validation and navigation
                        val qrRouteWithArg = AppDestinations.QR_SCAN_ROUTE.replace("{amount}", uiState.amount)
                        popUpTo(qrRouteWithArg) { inclusive = true }
                    }
                    // Reset the ViewModel state AFTER triggering navigation
                    qrViewModel.onNavigationHandled()
                }
            }

            // Display the QrScanScreen UI
            QrScanScreen(
                viewModel = qrViewModel, // Pass the VM instance down if needed (e.g., for errors)
                onNavigateBack = { navController.popBackStack() }
                // REMOVED: onQrScanSuccessNavigation - Navigation is handled above based on ViewModel state
            )
        }

        // --- Transaction Confirmation Screen ---
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
                amount = amount, beneficiaryId = beneficiaryId, beneficiaryName = beneficiaryName,
                onNavigateBack = { navController.popBackStack() },
                onConfirmAndProcess = { navAmount, navBeneficiaryId, navBeneficiaryName, navCategory ->
                    Log.d("AppNavigation", "Navigating to PIN Entry: A=$navAmount, C=$navCategory")
                    val pinEntryRoute = AppDestinations.createPinEntryRoute(navAmount, navBeneficiaryId, navBeneficiaryName, navCategory)
                    navController.navigate(pinEntryRoute)
                }
            )
        }

        // --- Pin Entry Screen ---
        composable(
            route = AppDestinations.PIN_ENTRY_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            PinEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onPinVerifiedNavigateToSuccess = { navAmount, navBeneficiaryId, navBeneficiaryName, navCategory ->
                    Log.d("AppNavigation", "PIN Verified. Navigating to Transaction Success.")
                    Toast.makeText(composableContext, "PIN Verified!", Toast.LENGTH_SHORT).show()
                    val mockTransactionId = "TXN-${System.currentTimeMillis()}"
                    val successRoute = AppDestinations.createTransactionSuccessRoute(navAmount, navBeneficiaryId, navBeneficiaryName, navCategory, mockTransactionId)
                    navController.navigate(successRoute) {
                        val pinEntryRouteWithArgs = AppDestinations.PIN_ENTRY_ROUTE_PATTERN
                            .replace("{amount}", navAmount)
                            .replace("{beneficiaryId}", navBeneficiaryId)
                            .replace("{beneficiaryName}", navBeneficiaryName)
                            .replace("{category}", navCategory)
                        popUpTo(pinEntryRouteWithArgs) { inclusive = true }

                        val confirmRoute = AppDestinations.TRANSACTION_CONFIRMATION_ROUTE
                            .replace("{amount}", navAmount)
                            .replace("{beneficiaryId}", navBeneficiaryId)
                            .replace("{beneficiaryName}", navBeneficiaryName)
                        popUpTo(confirmRoute) { inclusive = true }
                    }
                }
            )
        }

        // --- Transaction Success Screen ---
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
            TransactionSuccessScreen(
                onNavigateToHome = {
                    Log.d("AppNavigation", "Transaction Success: Navigating to Main.")
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

    } // End NavHost
} // End AppNavigation
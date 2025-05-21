// File: app/src/main/java/com/example/merchantapp/MainActivity.kt
package com.example.merchantapp

import android.app.Application
import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.merchantapp.data.local.AuthManager
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
import com.example.merchantapp.ui.summary.TransactionHistoryScreen // Ensure this path is correct
import com.example.merchantapp.ui.summary.TransactionHistoryViewModelFactory // Ensure this path is correct
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.ui.transactionsuccess.TransactionSuccessScreen
import com.example.merchantapp.viewmodel.QrScanViewModel
import com.example.merchantapp.viewmodel.TransactionHistoryViewModel


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
                    AppNavigation(applicationContext = this.applicationContext)
                }
            }
        }
        Log.d("MainActivity", "onCreate finished")
    }
}

@Composable
fun AppNavigation(applicationContext: Context) {
    val navController = rememberNavController()
    val composableContext = LocalContext.current

    val startDestination = remember {
        if (AuthManager.isLoggedIn(applicationContext)) {
            AppDestinations.MAIN_ROUTE
        } else {
            AppDestinations.LOGIN_ROUTE
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
                    navController.navigate(AppDestinations.FORGOT_PASSWORD_ROUTE)
                }
            )
        }

        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AppDestinations.FORGOT_PASSWORD_ROUTE) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onOtpSentNavigateToOtpEntry = { email ->
                    Toast.makeText(composableContext, "OTP 'sent' (Mocked: 111111)", Toast.LENGTH_LONG).show()
                    navController.navigate(AppDestinations.createOtpEntryRoute(email)) {
                        popUpTo(AppDestinations.FORGOT_PASSWORD_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestinations.OTP_ENTRY_ROUTE_PATTERN,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            OtpEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onOtpVerifiedNavigateToSetPassword = { verifiedEmail ->
                    Toast.makeText(composableContext, "OTP Verified!", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppDestinations.createSetNewPasswordRoute(verifiedEmail)) {
                        // Use the pattern for popUpTo if it contains arguments
                        popUpTo(AppDestinations.OTP_ENTRY_ROUTE_PATTERN.replace("{email}", verifiedEmail)) { inclusive = true }
                    }
                },
                onRequestResendOtp = { /* ... */ }
            )
        }

        composable(
            route = AppDestinations.SET_NEW_PASSWORD_ROUTE_PATTERN,
            arguments = listOf(navArgument("email_or_token") { type = NavType.StringType })
        ) {
            SetNewPasswordScreen(
                onPasswordSetNavigateToLogin = {
                    Toast.makeText(composableContext, "Password reset successfully!", Toast.LENGTH_LONG).show()
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true } // Or popUpTo graph start
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen(
                onLogoutRequest = {
                    AuthManager.logout(applicationContext)
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToQrScan = { amount ->
                    navController.navigate(AppDestinations.createQrScanRoute(amount))
                },
                onNavigateToTransactionHistory = { // This was the error: MainScreen needs this parameter
                    navController.navigate(AppDestinations.TRANSACTION_HISTORY_ROUTE)
                }
            )
        }

        composable(
            // Use PATTERN constant from AppDestinations
            route = AppDestinations.QR_SCAN_ROUTE_PATTERN,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) {
            val qrViewModel: QrScanViewModel = viewModel()
            val uiState by qrViewModel.uiState.collectAsState()

            LaunchedEffect(uiState.validationSuccess, uiState.validatedBeneficiary) {
                if (uiState.validationSuccess && uiState.validatedBeneficiary != null) {
                    val beneficiary = uiState.validatedBeneficiary!!
                    val route = AppDestinations.createTransactionConfirmationRoute(
                        amount = uiState.amount,
                        beneficiaryId = beneficiary.id,
                        beneficiaryName = beneficiary.name
                    )
                    navController.navigate(route) {
                        // Use PATTERN constant for popUpTo if it contains arguments
                        popUpTo(AppDestinations.QR_SCAN_ROUTE_PATTERN.replace("{amount}", uiState.amount)) { inclusive = true }
                    }
                    qrViewModel.onNavigationHandled()
                }
            }

            QrScanScreen(
                viewModel = qrViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            // Use PATTERN constant
            route = AppDestinations.TRANSACTION_CONFIRMATION_ROUTE_PATTERN,
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
                    val pinEntryRoute = AppDestinations.createPinEntryRoute(navAmount, navBeneficiaryId, navBeneficiaryName, navCategory)
                    navController.navigate(pinEntryRoute)
                }
            )
        }

        // Within AppNavigation composable in MainActivity.kt

        // --- Pin Entry Screen ---
        composable(
            route = AppDestinations.PIN_ENTRY_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { // Removed backStackEntry as it's not directly used here, PinEntryViewModel gets args
            PinEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                // This lambda now matches the 5 arguments we intend for PinEntryScreen to eventually provide
                onPinVerifiedNavigateToSuccess = { amount, beneficiaryId, beneficiaryName, category, transactionId ->
                    Log.d("AppNavigation", "PIN Verified. Navigating to Transaction Success. TxID: $transactionId")
                    // Ensure AppDestinations.createTransactionSuccessRoute expects transactionId first
                    val successRoute = AppDestinations.createTransactionSuccessRoute(
                        transactionId = transactionId,
                        amount = amount,
                        beneficiaryName = beneficiaryName, // Make sure this is the correct beneficiaryName to display on success screen
                        category = category
                    )
                    navController.navigate(successRoute) {
                        // Pop up to PIN entry route correctly
                        val currentPinEntryRoute = AppDestinations.PIN_ENTRY_ROUTE_PATTERN
                            .replace("{amount}", amount)
                            .replace("{beneficiaryId}", beneficiaryId)
                            .replace("{beneficiaryName}", beneficiaryName)
                            .replace("{category}", category)
                        popUpTo(currentPinEntryRoute) { inclusive = true }

                        // Also pop up to Transaction Confirmation route correctly
                        val currentConfirmRoute = AppDestinations.TRANSACTION_CONFIRMATION_ROUTE_PATTERN
                            .replace("{amount}", amount)
                            .replace("{beneficiaryId}", beneficiaryId)
                            .replace("{beneficiaryName}", beneficiaryName)
                        popUpTo(currentConfirmRoute) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestinations.TRANSACTION_SUCCESS_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("transactionId") {type = NavType.StringType }, // transactionId first
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) {
            TransactionSuccessScreen(
                onNavigateToHome = {
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.TRANSACTION_HISTORY_ROUTE) {
            val app = LocalContext.current.applicationContext as Application
            TransactionHistoryScreen(
                // viewModel = viewModel(factory = TransactionHistoryViewModelFactory(app)) // This line provides the viewModel
            )
            // If TransactionHistoryScreen takes onNavigateBack, you'd pass it here:
            // onNavigateBack = { navController.popBackStack() }
        }

    } // End NavHost
} // End AppNavigation
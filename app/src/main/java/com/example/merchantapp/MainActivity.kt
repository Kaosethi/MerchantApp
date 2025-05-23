package com.example.merchantapp

import android.app.Application
import android.content.Context
import android.net.Uri // For Uri.decode
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
// SavedStateHandle is used by the factory, not directly here for VM creation with AbstractSavedStateViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.merchantapp.auth.AuthEvent
import com.example.merchantapp.auth.AuthEventBus
import com.example.merchantapp.data.local.AuthManager
import com.example.merchantapp.navigation.AppDestinations
import com.example.merchantapp.navigation.AppNavigationActions
import com.example.merchantapp.navigation.BottomNavDestinations

// Screen Imports
import com.example.merchantapp.ui.confirmation.TransactionConfirmationScreen
import com.example.merchantapp.ui.forgotpassword.ForgotPasswordScreen
import com.example.merchantapp.ui.login.LoginScreen
import com.example.merchantapp.ui.main.MainScreen
import com.example.merchantapp.ui.otp.OtpEntryScreen
import com.example.merchantapp.ui.pinentry.PinEntryScreen
import com.example.merchantapp.ui.pinentry.PinEntryViewModelFactory // ViewModelFactory import
import com.example.merchantapp.ui.qr.QrScanScreen
import com.example.merchantapp.ui.register.RegisterScreen
import com.example.merchantapp.ui.setnewpassword.SetNewPasswordScreen
import com.example.merchantapp.ui.summary.TransactionHistoryScreen
import com.example.merchantapp.ui.summary.TransactionHistoryViewModelFactory
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.ui.transactionsuccess.TransactionSuccessScreen
import com.example.merchantapp.ui.outcome.TransactionOutcomeScreen
import com.example.merchantapp.ui.outcome.OutcomeType

// ViewModel Imports
import com.example.merchantapp.viewmodel.ForgotPasswordViewModel
import com.example.merchantapp.viewmodel.LoginViewModel
import com.example.merchantapp.viewmodel.OtpEntryViewModel
import com.example.merchantapp.viewmodel.PinEntryViewModel
import com.example.merchantapp.viewmodel.QrScanViewModel
import com.example.merchantapp.viewmodel.RegisterViewModel
import com.example.merchantapp.viewmodel.SetNewPasswordViewModel
import com.example.merchantapp.viewmodel.TransactionHistoryViewModel

import kotlinx.coroutines.flow.collectLatest

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
                    AppNavigation(applicationContext = applicationContext)
                }
            }
        }
        Log.d("MainActivity", "onCreate finished")
    }
}

@Composable
fun AppNavigation(applicationContext: Context) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) { AppNavigationActions(navController) }

    LaunchedEffect(Unit) {
        Log.i("AppNavigation", "AuthEventBus observer effect launched.")
        AuthEventBus.events.collectLatest { event ->
            Log.i("AppNavigation", "AuthEventBus received event: $event")
            when (event) {
                is AuthEvent.TokenExpiredOrInvalid -> {
                    if (!AuthManager.isLoggedIn(applicationContext)) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        Log.i("AppNavigation", "Handling TokenExpiredOrInvalid. Current route: $currentRoute.")
                        if (currentRoute != AppDestinations.LOGIN_ROUTE) {
                            navigationActions.navigateToLogin()
                            Log.i("AppNavigation", "Navigation to LOGIN_ROUTE initiated due to TokenExpiredOrInvalid.")
                            Toast.makeText(applicationContext, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    val startDestination = remember {
        if (AuthManager.isLoggedIn(applicationContext)) {
            AppDestinations.MAIN_ROUTE
        } else {
            AppDestinations.LOGIN_ROUTE
        }
    }
    Log.d("AppNavigation", "Start Destination: $startDestination, isLoggedIn: ${AuthManager.isLoggedIn(applicationContext)}")

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = { navigationActions.navigateToRegister() },
                onLoginSuccess = { navigationActions.navigateToMainScreenAfterLogin() },
                onNavigateToForgotPassword = { navigationActions.navigateToForgotPassword() }
            )
        }

        composable(AppDestinations.REGISTER_ROUTE) {
            val registerViewModel: RegisterViewModel = viewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateBack = { navigationActions.navigateUp() }
            )
        }

        composable(AppDestinations.FORGOT_PASSWORD_ROUTE) {
            val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
            ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onNavigateBack = { navigationActions.navigateUp() },
                onOtpSentNavigateToOtpEntry = { email ->
                    Toast.makeText(applicationContext, "OTP 'sent' (Mocked: 111111)", Toast.LENGTH_LONG).show()
                    navigationActions.navigateToOtpEntry(email)
                }
            )
        }

        composable(
            route = AppDestinations.OTP_ENTRY_ROUTE_PATTERN,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            val otpViewModel: OtpEntryViewModel = viewModel()
            OtpEntryScreen(
                viewModel = otpViewModel,
                onNavigateBack = { navigationActions.navigateUp() },
                onOtpVerifiedNavigateToSetPassword = { verifiedEmailOrToken ->
                    navigationActions.navigateToSetNewPassword(verifiedEmailOrToken)
                },
                onRequestResendOtp = { /* ViewModel handles this */ }
            )
        }

        composable(
            route = AppDestinations.SET_NEW_PASSWORD_ROUTE_PATTERN,
            arguments = listOf(navArgument("email_or_token") { type = NavType.StringType })
        ) {
            val setNewPasswordViewModel: SetNewPasswordViewModel = viewModel()
            SetNewPasswordScreen(
                viewModel = setNewPasswordViewModel,
                onPasswordSetNavigateToLogin = { navigationActions.navigateToLogin() }
            )
        }

        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen(
                onLogoutRequest = {
                    AuthManager.logout(applicationContext)
                    navigationActions.navigateToLogin()
                },
                onNavigateToQrScan = { amount -> navigationActions.navigateToQrScan(amount) }
            )
        }

        composable(
            route = AppDestinations.QR_SCAN_ROUTE_PATTERN,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val qrViewModel: QrScanViewModel = viewModel() // Assumes SSH handled by default or via Hilt
            val uiState by qrViewModel.uiState.collectAsState()

            LaunchedEffect(uiState.validationSuccess, uiState.validatedBeneficiary, navController) {
                if (uiState.validationSuccess && uiState.validatedBeneficiary != null) {
                    val beneficiary = uiState.validatedBeneficiary!!
                    val amountFromViewModel = uiState.amount
                    navigationActions.navigateToTransactionConfirmation(
                        amount = amountFromViewModel,
                        beneficiaryId = beneficiary.id,
                        beneficiaryName = beneficiary.name
                    )
                    qrViewModel.onNavigationHandled()
                }
            }
            QrScanScreen(
                viewModel = qrViewModel,
                onNavigateBack = { navigationActions.navigateUp() }
            )
        }

        composable(
            route = AppDestinations.TRANSACTION_CONFIRMATION_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount")?.let { Uri.decode(it) } ?: "Error"
            val beneficiaryId = backStackEntry.arguments?.getString("beneficiaryId")?.let { Uri.decode(it) } ?: "Error"
            val beneficiaryName = backStackEntry.arguments?.getString("beneficiaryName")?.let { Uri.decode(it) } ?: "Error"

            TransactionConfirmationScreen(
                amount = amount,
                beneficiaryId = beneficiaryId,
                beneficiaryName = beneficiaryName,
                onNavigateBack = { navigationActions.navigateUp() },
                onConfirmAndProcess = { confirmedAmount, confirmedBeneficiaryId, confirmedBeneficiaryName, confirmedCategory ->
                    navigationActions.navigateToPinEntry(
                        confirmedAmount,
                        confirmedBeneficiaryId,
                        confirmedBeneficiaryName,
                        confirmedCategory
                    )
                }
            )
        }

        // --- MODIFIED PinEntryScreen composable block ---
        composable(
            route = AppDestinations.PIN_ENTRY_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry -> // NavBackStackEntry is the SavedStateRegistryOwner
            val currentApplication = LocalContext.current.applicationContext as Application

            // Instantiate PinEntryViewModel using the updated factory
            val pinEntryViewModel: PinEntryViewModel = viewModel(
                viewModelStoreOwner = backStackEntry, // Scope ViewModel to this NavBackStackEntry
                factory = PinEntryViewModelFactory(
                    owner = backStackEntry, // Pass the NavBackStackEntry as the owner
                    application = currentApplication,
                    defaultArgs = backStackEntry.arguments // Pass the navigation arguments as defaultArgs
                )
            )

            // Log arguments from the ViewModel's SavedStateHandle (ensure PinEntryViewModel logs this in init)
            LaunchedEffect(pinEntryViewModel) {
                Log.d("AppNavigation/PinEntry", "PinEntryViewModel created. Check ViewModel logs for SSH content.")
            }

            PinEntryScreen(
                viewModel = pinEntryViewModel, // Pass the created ViewModel instance
                onNavigateBack = { navigationActions.navigateUp() },
                onPinVerifiedNavigateToSuccess = { amount, beneficiaryIdFromVM, beneficiaryNameFromVM, categoryFromVM, transactionId ->
                    navigationActions.navigateToTransactionSuccess(
                        transactionId = transactionId,
                        amount = amount,
                        beneficiaryId = beneficiaryIdFromVM,
                        beneficiaryName = beneficiaryNameFromVM,
                        category = categoryFromVM
                    )
                },
                onNavigateToOutcome = { outcomeType, title, message, buttonLabel ->
                    navigationActions.navigateToTransactionOutcome(
                        outcomeType, title, message, buttonLabel
                    )
                }
            )
        }
        // --- END MODIFIED PinEntryScreen composable block ---

        composable(
            route = AppDestinations.TRANSACTION_SUCCESS_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType },
                navArgument("beneficiaryId") { type = NavType.StringType },
                navArgument("beneficiaryName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")?.let { Uri.decode(it) } ?: "N/A"
            val amount = backStackEntry.arguments?.getString("amount")?.let { Uri.decode(it) } ?: "0.00"
            val beneficiaryAccountId = backStackEntry.arguments?.getString("beneficiaryId")?.let { Uri.decode(it) } ?: "N/A"
            val beneficiaryName = backStackEntry.arguments?.getString("beneficiaryName")?.let { Uri.decode(it) } ?: "N/A"
            val category = backStackEntry.arguments?.getString("category")?.let { Uri.decode(it) } ?: "N/A"

            TransactionSuccessScreen(
                transactionId = transactionId,
                amount = amount,
                beneficiaryName = beneficiaryName,
                category = category,
                beneficiaryAccountId = beneficiaryAccountId,
                onNavigateToHome = {
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = AppDestinations.TRANSACTION_OUTCOME_ROUTE_PATTERN,
            arguments = listOf(
                navArgument("outcomeType") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("message") { type = NavType.StringType },
                navArgument("buttonLabel") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val outcomeTypeName = backStackEntry.arguments?.getString("outcomeType")
            val title = backStackEntry.arguments?.getString("title")?.let { Uri.decode(it) } ?: "Error"
            val message = backStackEntry.arguments?.getString("message")?.let { Uri.decode(it) } ?: "An unexpected error occurred."
            val buttonLabel = backStackEntry.arguments?.getString("buttonLabel")?.let { Uri.decode(it) } ?: "OK"

            val outcomeType = try {
                OutcomeType.valueOf(outcomeTypeName ?: OutcomeType.ERROR_GENERAL.name)
            } catch (e: IllegalArgumentException) {
                Log.e("AppNavigation", "Invalid OutcomeType string: $outcomeTypeName", e)
                OutcomeType.ERROR_GENERAL
            }

            TransactionOutcomeScreen(
                outcomeType = outcomeType,
                titleText = title,
                messageText = message,
                buttonText = buttonLabel,
                onAcknowledge = {
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.TRANSACTION_HISTORY_ROUTE) {
            val app = LocalContext.current.applicationContext as Application
            val historyViewModel: TransactionHistoryViewModel = viewModel(
                factory = TransactionHistoryViewModelFactory(app)
            )
            TransactionHistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = { navigationActions.navigateUp() }
            )
        }
    }
}
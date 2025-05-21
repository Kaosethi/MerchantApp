package com.example.merchantapp.navigation

import androidx.navigation.NavHostController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main" // This will be your screen with BottomNavigation
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"

    // Routes for OTP and Set Password screens
    const val OTP_ENTRY_ROUTE_PATTERN = "otp_entry/{email}"
    const val SET_NEW_PASSWORD_ROUTE_PATTERN = "set_new_password/{email_or_token}"

    // Transaction Flow Routes
    const val QR_SCAN_ROUTE_PATTERN = "qr_scan/{amount}" // Renamed for consistency
    const val TRANSACTION_CONFIRMATION_ROUTE_PATTERN = "transaction_confirmation/{amount}/{beneficiaryId}/{beneficiaryName}"
    const val PIN_ENTRY_ROUTE_PATTERN = "pin_entry/{amount}/{beneficiaryId}/{beneficiaryName}/{category}"
    const val TRANSACTION_SUCCESS_ROUTE_PATTERN = "transaction_success/{transactionId}/{amount}/{beneficiaryName}/{category}" // Reordered for clarity

    // Route for Transaction History Screen (within MainScreen's BottomNav or accessible from MainScreen)
    const val TRANSACTION_HISTORY_ROUTE = "transaction_history" // Does not require arguments

    // --- Helper functions to create routes with arguments ---

    fun createOtpEntryRoute(email: String): String {
        val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
        return "otp_entry/$encodedEmail"
    }

    fun createSetNewPasswordRoute(identifier: String): String {
        val encodedIdentifier = URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString())
        return "set_new_password/$encodedIdentifier"
    }

    fun createQrScanRoute(amount: String): String {
        // Amount usually doesn't need encoding if it's just numbers/decimal point
        return "qr_scan/$amount"
    }

    fun createTransactionConfirmationRoute(amount: String, beneficiaryId: String, beneficiaryName: String): String {
        val encodedBeneficiaryId = URLEncoder.encode(beneficiaryId, StandardCharsets.UTF_8.toString())
        val encodedBeneficiaryName = URLEncoder.encode(beneficiaryName, StandardCharsets.UTF_8.toString())
        return "transaction_confirmation/$amount/$encodedBeneficiaryId/$encodedBeneficiaryName"
    }

    fun createPinEntryRoute(amount: String, beneficiaryId: String, beneficiaryName: String, category: String): String {
        val encodedBeneficiaryId = URLEncoder.encode(beneficiaryId, StandardCharsets.UTF_8.toString())
        val encodedBeneficiaryName = URLEncoder.encode(beneficiaryName, StandardCharsets.UTF_8.toString())
        val encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
        return "pin_entry/$amount/$encodedBeneficiaryId/$encodedBeneficiaryName/$encodedCategory"
    }

    fun createTransactionSuccessRoute(transactionId: String, amount: String, beneficiaryName: String, category: String): String {
        val encodedTransactionId = URLEncoder.encode(transactionId, StandardCharsets.UTF_8.toString())
        val encodedBeneficiaryName = URLEncoder.encode(beneficiaryName, StandardCharsets.UTF_8.toString())
        val encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
        return "transaction_success/$encodedTransactionId/$amount/$encodedBeneficiaryName/$encodedCategory"
    }
}

// This object defines routes that are part of the Bottom Navigation within MainScreen
object BottomNavDestinations {
    const val AMOUNT_ENTRY = "amount_entry_tab" // Route for the Amount Entry tab/screen
    const val TRANSACTION_HISTORY = AppDestinations.TRANSACTION_HISTORY_ROUTE // Re-use constant
    const val ANALYTICS = "analytics_tab"       // Route for the Analytics tab/screen
    // Add other bottom nav destinations here if needed
}


// AppNavigationActions class:
// Provides type-safe navigation actions.
// Useful if you want to centralize navigation logic, especially from ViewModels
// or places where direct NavController access is less convenient.
// For simple navigation from Composables, direct navController.navigate() is often fine.
class AppNavigationActions(private val navController: NavHostController) {

    fun navigateToLogin() {
        navController.navigate(AppDestinations.LOGIN_ROUTE) {
            // Example: Pop up to the start destination of the graph to avoid building up a large back stack.
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateToRegister() {
        navController.navigate(AppDestinations.REGISTER_ROUTE)
    }

    fun navigateToMainScreenAfterLogin() {
        navController.navigate(AppDestinations.MAIN_ROUTE) {
            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateToForgotPassword() {
        navController.navigate(AppDestinations.FORGOT_PASSWORD_ROUTE)
    }

    fun navigateToOtpEntry(email: String) {
        navController.navigate(AppDestinations.createOtpEntryRoute(email))
    }

    fun navigateToSetNewPassword(identifier: String) {
        navController.navigate(AppDestinations.createSetNewPasswordRoute(identifier))
    }

    fun navigateToQrScan(amount: String) {
        navController.navigate(AppDestinations.createQrScanRoute(amount))
    }

    fun navigateToTransactionConfirmation(amount: String, beneficiaryId: String, beneficiaryName: String) {
        navController.navigate(AppDestinations.createTransactionConfirmationRoute(amount, beneficiaryId, beneficiaryName))
    }

    fun navigateToPinEntry(amount: String, beneficiaryId: String, beneficiaryName: String, category: String) {
        navController.navigate(AppDestinations.createPinEntryRoute(amount, beneficiaryId, beneficiaryName, category))
    }

    fun navigateToTransactionSuccess(transactionId: String, amount: String, beneficiaryName: String, category: String) {
        navController.navigate(AppDestinations.createTransactionSuccessRoute(transactionId, amount, beneficiaryName, category))
    }

    fun navigateToTransactionHistory() {
        navController.navigate(AppDestinations.TRANSACTION_HISTORY_ROUTE)
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}
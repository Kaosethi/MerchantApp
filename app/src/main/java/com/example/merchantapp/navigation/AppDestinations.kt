// File: app/src/main/java/com/example/merchantapp/navigation/AppDestinations.kt
package com.example.merchantapp.navigation

import androidx.navigation.NavHostController // Ensure this import is present

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"

    // Routes for OTP and Set Password screens
    const val OTP_ENTRY_ROUTE_PATTERN = "otp_entry/{email}" // email is a path argument
    const val SET_NEW_PASSWORD_ROUTE_PATTERN = "set_new_password/{email_or_token}" // email or token

    // Original Routes
    const val QR_SCAN_ROUTE = "qr_scan/{amount}"
    const val TRANSACTION_CONFIRMATION_ROUTE = "transaction_confirmation/{amount}/{beneficiaryId}/{beneficiaryName}"

    // ADDED: Route pattern for PIN Entry Screen
    const val PIN_ENTRY_ROUTE_PATTERN = "pin_entry/{amount}/{beneficiaryId}/{beneficiaryName}/{category}"

    // ADDED: Route pattern for Transaction Success Screen (placeholder for now)
    const val TRANSACTION_SUCCESS_ROUTE_PATTERN = "transaction_success/{amount}/{beneficiaryId}/{beneficiaryName}/{category}/{transactionId}"


    // Helper function to create the OTP entry route with email argument
    fun createOtpEntryRoute(email: String): String = "otp_entry/$email"

    // Helper function for Set New Password route
    fun createSetNewPasswordRoute(identifier: String): String = "set_new_password/$identifier"

    // Original helper functions
    fun createQrScanRoute(amount: String): String = "qr_scan/$amount"

    fun createTransactionConfirmationRoute(amount: String, beneficiaryId: String, beneficiaryName: String): String {
        return "transaction_confirmation/$amount/$beneficiaryId/$beneficiaryName"
    }

    // ADDED: Helper function for PIN Entry route
    fun createPinEntryRoute(amount: String, beneficiaryId: String, beneficiaryName: String, category: String): String {
        // Note: Ensure category is URL-encoded if it can contain special characters.
        // For simplicity now, assuming it's a simple string.
        return "pin_entry/$amount/$beneficiaryId/$beneficiaryName/$category"
    }

    // ADDED: Helper function for Transaction Success route (placeholder for now)
    fun createTransactionSuccessRoute(amount: String, beneficiaryId: String, beneficiaryName: String, category: String, transactionId: String): String {
        return "transaction_success/$amount/$beneficiaryId/$beneficiaryName/$category/$transactionId"
    }
}

// Re-included BottomNavScreens object
object BottomNavScreens {
    const val AMOUNT_ENTRY = "amount_entry"
    const val SUMMARY = "summary"
    const val ANALYTICS = "analytics"
}

// Re-included AppNavigationActions class
class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToQrScan(amount: String) {
        navController.navigate(AppDestinations.createQrScanRoute(amount))
    }
    // Add other navigation actions here if needed in the future
}
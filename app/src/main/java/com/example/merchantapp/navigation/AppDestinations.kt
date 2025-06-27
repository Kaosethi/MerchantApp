package com.example.merchantapp.navigation

import android.net.Uri
import android.util.Log
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.merchantapp.ui.outcome.OutcomeType

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"

    const val OTP_ENTRY_ROUTE_PATTERN = "otp_entry/{email}" // Key: "email"
    // MODIFIED: Route pattern to accept two distinct arguments
    const val SET_NEW_PASSWORD_ROUTE_PATTERN = "set_new_password/{email}/{resetAuthToken}" // Keys: "email", "resetAuthToken"

    const val QR_SCAN_ROUTE_PATTERN = "qr_scan/{amount}"
    const val TRANSACTION_CONFIRMATION_ROUTE_PATTERN =
        "transaction_confirmation/{transactionAmount}/{beneficiaryId}/{beneficiaryName}/{beneficiaryDisplayId}"
    const val PIN_ENTRY_ROUTE_PATTERN =
        "pin_entry/{transactionAmount}/{beneficiaryId}/{beneficiaryName}/{beneficiaryDisplayId}/{category}"
    const val TRANSACTION_SUCCESS_ROUTE_PATTERN = "transaction_success/{transactionId}/{amount}/{beneficiaryId}/{beneficiaryName}/{category}"
    const val TRANSACTION_OUTCOME_ROUTE_PATTERN = "transaction_outcome/{outcomeType}/{title}/{message}/{buttonLabel}"

    const val DASHBOARD_ROUTE = "dashboard"
    const val TRANSACTION_HISTORY_ROUTE = "transaction_history"
    const val AMOUNT_ENTRY_ROUTE = "amount_entry"

    fun createOtpEntryRoute(email: String): String {
        return OTP_ENTRY_ROUTE_PATTERN.replace("{email}", Uri.encode(email))
    }

    // MODIFIED: Helper function to create route with email and resetAuthToken
    fun createSetNewPasswordRoute(email: String, resetAuthToken: String): String {
        return SET_NEW_PASSWORD_ROUTE_PATTERN
            .replace("{email}", Uri.encode(email))
            .replace("{resetAuthToken}", Uri.encode(resetAuthToken))
    }

    fun createQrScanRoute(amount: String): String {
        return QR_SCAN_ROUTE_PATTERN.replace("{amount}", Uri.encode(amount))
    }

    fun createTransactionConfirmationRoute(
        transactionAmount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        beneficiaryDisplayId: String
    ): String {
        return TRANSACTION_CONFIRMATION_ROUTE_PATTERN
            .replace("{transactionAmount}", Uri.encode(transactionAmount))
            .replace("{beneficiaryId}", Uri.encode(beneficiaryId))
            .replace("{beneficiaryName}", Uri.encode(beneficiaryName))
            .replace("{beneficiaryDisplayId}", Uri.encode(beneficiaryDisplayId))
    }

    fun createPinEntryRoute(
        transactionAmount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        beneficiaryDisplayId: String,
        category: String
    ): String {
        return PIN_ENTRY_ROUTE_PATTERN
            .replace("{transactionAmount}", Uri.encode(transactionAmount))
            .replace("{beneficiaryId}", Uri.encode(beneficiaryId))
            .replace("{beneficiaryName}", Uri.encode(beneficiaryName))
            .replace("{beneficiaryDisplayId}", Uri.encode(beneficiaryDisplayId))
            .replace("{category}", Uri.encode(category))
    }

    fun createTransactionSuccessRoute(
        transactionId: String,
        amount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        category: String
    ): String {
        return TRANSACTION_SUCCESS_ROUTE_PATTERN
            .replace("{transactionId}", Uri.encode(transactionId))
            .replace("{amount}", Uri.encode(amount))
            .replace("{beneficiaryId}", Uri.encode(beneficiaryId))
            .replace("{beneficiaryName}", Uri.encode(beneficiaryName))
            .replace("{category}", Uri.encode(category))
    }

    fun createTransactionOutcomeRoute(
        outcomeType: OutcomeType,
        title: String,
        message: String,
        buttonLabel: String
    ): String {
        return TRANSACTION_OUTCOME_ROUTE_PATTERN
            .replace("{outcomeType}", outcomeType.name)
            .replace("{title}", Uri.encode(title))
            .replace("{message}", Uri.encode(message))
            .replace("{buttonLabel}", Uri.encode(buttonLabel))
    }
}

object BottomNavDestinations {
    const val DASHBOARD_TAB = "app_dashboard_tab"
    const val AMOUNT_ENTRY_TAB = "app_amount_entry_tab"
    const val TRANSACTION_HISTORY_TAB = "app_transaction_history_tab"
}

class AppNavigationActions(private val navController: NavHostController) {

    // MODIFIED: Added clearBackStack parameter with a default value
    fun navigateToLogin(clearBackStack: Boolean = false) {
        navController.navigate(AppDestinations.LOGIN_ROUTE) {
            if (clearBackStack) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
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

    // MODIFIED: Function now accepts email and resetToken
    fun navigateToSetNewPassword(email: String, resetToken: String) {
        navController.navigate(AppDestinations.createSetNewPasswordRoute(email, resetToken))
    }

    fun navigateToQrScan(amount: String) {
        navController.navigate(AppDestinations.createQrScanRoute(amount))
    }

    fun navigateToTransactionConfirmation(
        amount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        beneficiaryDisplayId: String
    ) {
        val route = AppDestinations.createTransactionConfirmationRoute(
            transactionAmount = amount,
            beneficiaryId = beneficiaryId,
            beneficiaryName = beneficiaryName,
            beneficiaryDisplayId = beneficiaryDisplayId // ✅ include this
        )
        navController.navigate(route)
    }

    fun navigateToPinEntry(
        amount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        beneficiaryDisplayId: String,
        category: String
    ) {
        val route = AppDestinations.createPinEntryRoute(
            transactionAmount = amount,
            beneficiaryId = beneficiaryId,
            beneficiaryName = beneficiaryName,
            beneficiaryDisplayId = beneficiaryDisplayId,
            category = category
        )
        navController.navigate(route)
    }

    fun navigateToTransactionSuccess(
        transactionId: String,
        amount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        category: String
    ) {
        val route = AppDestinations.createTransactionSuccessRoute(
            transactionId, amount, beneficiaryId, beneficiaryName, category
        )
        Log.d("AppNavActions", "Navigating to TransactionSuccess route: $route")
        navController.navigate(route) {
            popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun navigateToTransactionOutcome(
        outcomeType: OutcomeType,
        title: String,
        message: String,
        buttonLabel: String
    ) {
        val route = AppDestinations.createTransactionOutcomeRoute(outcomeType, title, message, buttonLabel)
        Log.d("AppNavActions", "Navigating to TransactionOutcome route: $route")
        navController.navigate(route) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && currentRoute.startsWith(AppDestinations.PIN_ENTRY_ROUTE_PATTERN.substringBefore("/{"))) {
                popUpTo(currentRoute) { inclusive = true }
            }
            launchSingleTop = true
        }
    }

    fun navigateToHomeAfterTransaction() {
        navController.navigate(AppDestinations.MAIN_ROUTE) {
            popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateToTransactionHistoryScreen() {
        navController.navigate(AppDestinations.TRANSACTION_HISTORY_ROUTE)
    }

    fun navigateUp() {
        navController.popBackStack()
    }
}
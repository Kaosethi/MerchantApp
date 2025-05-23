package com.example.merchantapp.navigation

import android.net.Uri // Changed from java.net.URLEncoder to android.net.Uri for consistency and better API
import android.util.Log
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
// Assuming OutcomeType will be defined in a place accessible here, e.g., com.example.merchantapp.ui.outcome.OutcomeType
// For now, we'll proceed as if it's available or handle it as String if not defined yet.
// If OutcomeType is defined, e.g., in com.example.merchantapp.ui.outcome.OutcomeType
import com.example.merchantapp.ui.outcome.OutcomeType // Placeholder for actual import

object AppDestinations {
    // Authentication & Main Structure
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main" // This likely represents the screen holding the BottomNav
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"

    // Password Reset Flow
    const val OTP_ENTRY_ROUTE_PATTERN = "otp_entry/{email}"
    const val SET_NEW_PASSWORD_ROUTE_PATTERN = "set_new_password/{email_or_token}"

    // Transaction Flow
    const val QR_SCAN_ROUTE_PATTERN = "qr_scan/{amount}"
    const val TRANSACTION_CONFIRMATION_ROUTE_PATTERN = "transaction_confirmation/{amount}/{beneficiaryId}/{beneficiaryName}"
    const val PIN_ENTRY_ROUTE_PATTERN = "pin_entry/{amount}/{beneficiaryId}/{beneficiaryName}/{category}"
    const val TRANSACTION_SUCCESS_ROUTE_PATTERN = "transaction_success/{transactionId}/{amount}/{beneficiaryId}/{beneficiaryName}/{category}"
    // --- NEW ROUTE PATTERN FOR TRANSACTION OUTCOME ---
    const val TRANSACTION_OUTCOME_ROUTE_PATTERN = "transaction_outcome/{outcomeType}/{title}/{message}/{buttonLabel}"


    // Standalone Screens or Tabs (These are also used as BottomNavDestinations)
    const val DASHBOARD_ROUTE = "dashboard" // Actual route name for dashboard screen
    const val TRANSACTION_HISTORY_ROUTE = "transaction_history" // Actual route name for history screen
    const val AMOUNT_ENTRY_ROUTE = "amount_entry" // Actual route name for amount entry screen


    // --- Helper functions to create routes with arguments ---
    // Using android.net.Uri.encode for consistency
    fun createOtpEntryRoute(email: String): String {
        return "otp_entry/${Uri.encode(email)}"
    }

    fun createSetNewPasswordRoute(identifier: String): String {
        return "set_new_password/${Uri.encode(identifier)}"
    }

    fun createQrScanRoute(amount: String): String {
        // Amount is usually a simple number, but encoding is harmless
        return "qr_scan/${Uri.encode(amount)}"
    }

    fun createTransactionConfirmationRoute(amount: String, beneficiaryId: String, beneficiaryName: String): String {
        return "transaction_confirmation/${Uri.encode(amount)}/${Uri.encode(beneficiaryId)}/${Uri.encode(beneficiaryName)}"
    }

    fun createPinEntryRoute(amount: String, beneficiaryId: String, beneficiaryName: String, category: String): String {
        return "pin_entry/${Uri.encode(amount)}/${Uri.encode(beneficiaryId)}/${Uri.encode(beneficiaryName)}/${Uri.encode(category)}"
    }

    fun createTransactionSuccessRoute(
        transactionId: String,
        amount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        category: String
    ): String {
        return "transaction_success/${Uri.encode(transactionId)}/${Uri.encode(amount)}/${Uri.encode(beneficiaryId)}/${Uri.encode(beneficiaryName)}/${Uri.encode(category)}"
    }

    // --- NEW HELPER FUNCTION FOR TRANSACTION OUTCOME ---
    fun createTransactionOutcomeRoute(
        outcomeType: OutcomeType, // Using the enum directly
        title: String,
        message: String,
        buttonLabel: String
    ): String {
        // Enum.name will give the string representation of the enum constant
        return "transaction_outcome/${outcomeType.name}/${Uri.encode(title)}/${Uri.encode(message)}/${Uri.encode(buttonLabel)}"
    }
}

object BottomNavDestinations {
    // These constants should match the 'route' argument used in the NavHost for the bottom navigation.
    // If DashboardScreen, AmountEntryScreen, TransactionHistoryScreen are direct children of the *main* NavHost,
    // then these can directly use AppDestinations.
    // However, you mentioned earlier `MainScreen.kt` has a *nested* NavHost for bottom navigation.
    // In that case, these routes are specific to that nested graph.
    // Let's assume the routes for the tabs are simple strings that the nested NavHost uses.
    // If AppDestinations.DASHBOARD_ROUTE (e.g., "dashboard") is indeed the route for the tab, this is fine.

    // Based on your MainScreen.kt, you used:
    // BottomNavDestinations.DASHBOARD_TAB
    // BottomNavDestinations.AMOUNT_ENTRY_TAB
    // BottomNavDestinations.TRANSACTION_HISTORY_TAB
    // Let's define them uniquely if they differ from AppDestinations, or align them if they are the same.
    // For clarity, I'll assume these are distinct strings for the bottom nav graph,
    // even if they load composables defined elsewhere.
    // If they are truly the same as AppDestinations, you can revert.

    const val DASHBOARD_TAB = "app_dashboard_tab" // Example distinct route for tab
    const val AMOUNT_ENTRY_TAB = "app_amount_entry_tab"
    const val TRANSACTION_HISTORY_TAB = "app_transaction_history_tab"
}

class AppNavigationActions(private val navController: NavHostController) {

    fun navigateToLogin() {
        navController.navigate(AppDestinations.LOGIN_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateToRegister() {
        navController.navigate(AppDestinations.REGISTER_ROUTE)
    }

    fun navigateToMainScreenAfterLogin() {
        // MAIN_ROUTE is likely where your MainScreen (with BottomNav) is hosted.
        navController.navigate(AppDestinations.MAIN_ROUTE) {
            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true } // Clear login screen
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

    // Navigation actions for screens within the transaction flow
    // These are typically part of the main NavHost graph, not the bottom nav's nested graph.

    fun navigateToQrScan(amount: String) {
        navController.navigate(AppDestinations.createQrScanRoute(amount))
    }

    fun navigateToTransactionConfirmation(amount: String, beneficiaryId: String, beneficiaryName: String) {
        navController.navigate(AppDestinations.createTransactionConfirmationRoute(amount, beneficiaryId, beneficiaryName))
    }

    fun navigateToPinEntry(amount: String, beneficiaryId: String, beneficiaryName: String, category: String) {
        navController.navigate(AppDestinations.createPinEntryRoute(amount, beneficiaryId, beneficiaryName, category))
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
            // Pop up to MAIN_ROUTE, which hosts the bottom navigation.
            // This clears the transaction flow (QR, Confirm, PIN) from the backstack.
            popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = false }
            launchSingleTop = true
        }
    }

    // --- NEW NAVIGATION ACTION FOR TRANSACTION OUTCOME ---
    fun navigateToTransactionOutcome(
        outcomeType: OutcomeType,
        title: String,
        message: String,
        buttonLabel: String
    ) {
        val route = AppDestinations.createTransactionOutcomeRoute(outcomeType, title, message, buttonLabel)
        Log.d("AppNavActions", "Navigating to TransactionOutcome route: $route")
        navController.navigate(route) {
            // When navigating to an outcome screen from PIN entry, we usually want to clear the PIN entry screen.
            // And the outcome screen's "Done" button will then pop back to MAIN_ROUTE.
            // So, pop up to the current destination (which is PinEntryScreen) and include it.
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && currentRoute.startsWith("pin_entry")) { // Be more specific if PinEntry is the only source
                popUpTo(currentRoute) { inclusive = true }
            }
            // If other screens could lead here, adjust popUpTo logic.
            // For now, assuming it's from PinEntry.
            launchSingleTop = true // Good practice for final/outcome screens.
        }
    }


    fun navigateToHomeAfterTransaction() { // This seems to be for the "Done" button on Success/Outcome
        navController.navigate(AppDestinations.MAIN_ROUTE) { // Or directly to BottomNavDestinations.DASHBOARD_TAB if navController is the main one
            popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true } // Clears everything including MAIN_ROUTE and rebuilds it
            launchSingleTop = true
        }
        // A more common pattern for "Done" on success/failure is to pop back to a stable point:
        // navController.popBackStack(AppDestinations.MAIN_ROUTE, inclusive = false)
        // This returns to the MainScreen instance without re-creating it, assuming it's on the backstack.
    }


    // This is for navigating to a tab using the main navController
    // This is if TransactionHistoryScreen is a top-level destination in the main NavHost
    fun navigateToTransactionHistoryScreen() {
        // Assuming TRANSACTION_HISTORY_ROUTE is a screen in the main graph, not just a tab
        navController.navigate(AppDestinations.TRANSACTION_HISTORY_ROUTE)
    }

    fun navigateUp() { // Renamed from navigateBack for clarity with Jetpack Navigation convention
        navController.popBackStack()
    }
}
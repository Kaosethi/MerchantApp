// File: app/src/main/java/com/example/merchantapp/navigation/AppDestinations.kt
package com.example.merchantapp.navigation

import androidx.navigation.NavHostController
// REMOVED: Imports related to PIN entry args and encoding

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
    // NOTE: These routes take RAW strings
    const val QR_SCAN_ROUTE = "qr_scan/{amount}"
    const val TRANSACTION_CONFIRMATION_ROUTE = "transaction_confirmation/{amount}/{beneficiaryId}/{beneficiaryName}"

    fun createQrScanRoute(amount: String): String = "qr_scan/$amount"

    // NOTE: Passes raw strings. Encoding wasn't explicitly handled here in the previously stable version.
    fun createTransactionConfirmationRoute(amount: String, beneficiaryId: String, beneficiaryName: String): String {
        // Simple concatenation. If encoding needed later, handle during navigation call.
        return "transaction_confirmation/$amount/$beneficiaryId/$beneficiaryName"
    }

    // REMOVED: PIN_ENTRY_ROUTE, pinEntryArguments, createPinEntryRoute
}

object BottomNavScreens {
    const val AMOUNT_ENTRY = "amount_entry"
    const val SUMMARY = "summary"
    const val ANALYTICS = "analytics"
}

// Reverted NavigationActions class
class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToQrScan(amount: String) {
        navController.navigate(AppDestinations.createQrScanRoute(amount))
    }
    // REMOVED: navigateToPinEntry method if it existed
}
package com.example.merchantapp.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

// Ensure only ONE definition of this object exists in the project
object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
    const val QR_SCAN_ROUTE = "qr_scan/{amount}" // Route with argument placeholder

    // Helper to create route with actual amount
    fun createQrScanRoute(amount: String): String = "qr_scan/$amount"
}

// Ensure only ONE definition of this object exists in the project
object BottomNavScreens {
    const val AMOUNT_ENTRY = "amount_entry"
    const val SUMMARY = "summary"
    const val ANALYTICS = "analytics"
}

// Optional helper class - ensure it's not defined elsewhere if unused
class AppNavigationActions(private val navController: NavHostController) {

    fun navigateToRegister() {
        navController.navigate(AppDestinations.REGISTER_ROUTE)
    }

    fun navigateToMain(builder: NavOptionsBuilder.() -> Unit = {}) {
        navController.navigate(AppDestinations.MAIN_ROUTE, builder)
    }

    fun navigateToQrScan(amount: String) {
        navController.navigate(AppDestinations.createQrScanRoute(amount))
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}
// File: app/src/main/java/com/example/merchantapp/navigation/Navigation.kt
package com.example.merchantapp.navigation

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main" // Route for the screen containing bottom nav
}

// Define routes for screens within the Main screen's bottom nav
object BottomNavScreens {
    const val AMOUNT_ENTRY = "amount_entry"
    const val SUMMARY = "summary"
    const val ANALYTICS = "analytics"
}
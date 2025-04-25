// File: app/src/main/java/com/example/merchantapp/MainActivity.kt
package com.example.merchantapp

// REMOVED imports related to language: SharedPreferences, Context, AppCompatDelegate, LocaleListCompat, KTX edit
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// androidx.appcompat.app.AppCompatDelegate - REMOVED (or keep if needed elsewhere)
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
// androidx.core.content.edit - REMOVED
// androidx.core.os.LocaleListCompat - REMOVED
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.merchantapp.navigation.AppDestinations
import com.example.merchantapp.ui.login.LoginScreen
import com.example.merchantapp.ui.main.MainScreen
import com.example.merchantapp.ui.register.RegisterScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme

// REMOVED Constants: PREFS_NAME, KEY_LANGUAGE, DEFAULT_LANGUAGE

class MainActivity : ComponentActivity() {

    // REMOVED: prefs variable

    override fun onCreate(savedInstanceState: Bundle?) {
        // REMOVED: Language application logic before super.onCreate
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called") // Simplified log

        enableEdgeToEdge()
        setContent {
            MerchantAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // REMOVED: Passing language state to AppNavigation
                    AppNavigation()
                }
            }
        }
    }

    // REMOVED: getCurrentLanguage function
    // REMOVED: saveLanguagePreference function
    // REMOVED: applyLocale function
    // REMOVED: changeLanguageAndRecreate function
}


// MODIFIED: Removed language parameters
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onNavigateToRegister = {
                    Log.d("AppNavigation", "Navigating to Register")
                    navController.navigate(AppDestinations.REGISTER_ROUTE)
                },
                onLoginSuccess = {
                    Log.d("AppNavigation", "Login Success: Navigating to Main")
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.REGISTER_ROUTE) {
            // MODIFIED: Removed language parameters from RegisterScreen call
            RegisterScreen(
                onNavigateBack = {
                    Log.d("AppNavigation", "Navigating back from Register")
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen(
                onLogoutRequest = {
                    Log.d("AppNavigation", "Logout Requested: Navigating to Login")
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
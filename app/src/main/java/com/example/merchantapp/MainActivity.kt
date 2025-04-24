// File: app/src/main/java/com/example/merchantapp/MainActivity.kt
package com.example.merchantapp

import android.os.Bundle
import android.util.Log
// import android.widget.Toast // Can remove if not used directly here anymore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable // ADDED
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost // ADDED
import androidx.navigation.compose.composable // ADDED
import androidx.navigation.compose.rememberNavController // ADDED
import com.example.merchantapp.navigation.AppDestinations // ADDED
import com.example.merchantapp.ui.login.LoginScreen
import com.example.merchantapp.ui.main.MainScreen // ADDED
import com.example.merchantapp.ui.register.RegisterScreen // ADDED
import com.example.merchantapp.ui.theme.MerchantAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MerchantAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Setup Navigation
                    AppNavigation() // Call the main navigation composable
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController() // Create NavController instance

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE // Start at the Login screen
    ) {
        // Login Screen Composable
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onNavigateToRegister = {
                    Log.d("AppNavigation", "Navigating to Register")
                    navController.navigate(AppDestinations.REGISTER_ROUTE)
                },
                onLoginSuccess = {
                    Log.d("AppNavigation", "Login Success: Navigating to Main")
                    // Navigate to main screen and clear login from back stack
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) {
                            inclusive = true // Remove Login screen from back stack
                        }
                        launchSingleTop = true // Avoid multiple copies of Main screen
                    }
                }
                // ViewModel is provided automatically by viewModel() within LoginScreen
            )
        }

        // Registration Screen Composable
        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateBack = {
                    Log.d("AppNavigation", "Navigating back from Register")
                    navController.popBackStack() // Simple back navigation
                }
            )
        }

        // Main Application Screen (with Bottom Navigation) Composable
        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen() // Pass navController if needed for logout etc.
            // MainScreen now manages its own internal navigation for bottom bar content
        }
    }
}
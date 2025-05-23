package com.example.merchantapp.ui.main

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AttachMoney // Changed from Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight // Import for FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Import for sp (font size)
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.merchantapp.navigation.BottomNavDestinations // Your BottomNavDestinations
import com.example.merchantapp.ui.amount.AmountEntryScreen // Your Amount Entry Screen
import com.example.merchantapp.ui.dashboard.DashboardScreen // Your Dashboard Screen
import com.example.merchantapp.ui.summary.TransactionHistoryScreen // Your History Screen
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.MainViewModel // Your MainViewModel

// ViewModel Factory for MainViewModel
class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for MainViewModelFactory")
    }
}

// Data class for bottom navigation items
data class BottomNavItemData(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogoutRequest: () -> Unit,
    onNavigateToQrScan: (amount: String) -> Unit,
    // onNavigateToTransactionHistory lambda removed from parameters, as tab click handles it
    mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val bottomNavHostController = rememberNavController()
    val mainUiState by mainViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.fetchMerchantProfile() // Fetch profile when screen launches
    }

    val topBarTitleText = mainUiState.merchantProfile?.businessName ?: "Merchant Portal"

    val bottomNavItems = listOf(
        BottomNavItemData(label = "Dashboard", icon = Icons.Filled.Dashboard, route = BottomNavDestinations.DASHBOARD_TAB),
        BottomNavItemData(label = "New Tx", icon = Icons.Filled.AttachMoney, route = BottomNavDestinations.AMOUNT_ENTRY_TAB),
        BottomNavItemData(label = "History", icon = Icons.Filled.History, route = BottomNavDestinations.TRANSACTION_HISTORY_TAB)
        // Analytics item has been removed
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // Using CenterAlignedTopAppBar
                title = {
                    Text(
                        text = topBarTitleText,
                        fontSize = 22.sp,      // Increased font size
                        fontWeight = FontWeight.Bold // Made text bold
                    )
                },
                actions = {
                    IconButton(onClick = onLogoutRequest) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
                // For "moving downward": The increased font size will make it more prominent.
                // Further adjustments would involve WindowInsets or custom TopAppBar layouts.
                // The TopAppBar is designed to sit at the top, respecting system status bar insets.
            )
        },
        bottomBar = {
            NavigationBar { // Material 3 NavigationBar
                val navBackStackEntry by bottomNavHostController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.route == item.route,
                        onClick = {
                            bottomNavHostController.navigate(item.route) {
                                popUpTo(bottomNavHostController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavHostController,
            startDestination = BottomNavDestinations.DASHBOARD_TAB, // Default to Dashboard tab
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        ) {
            composable(BottomNavDestinations.DASHBOARD_TAB) {
                DashboardScreen(navController = bottomNavHostController) // Pass bottomNavController
            }
            composable(BottomNavDestinations.AMOUNT_ENTRY_TAB) {
                AmountEntryScreen(
                    onNavigateToQrScan = onNavigateToQrScan // This uses the main app's NavController action
                    // If AmountEntryScreen needs its own internal navigation:
                    // navController = bottomNavHostController
                )
            }
            composable(BottomNavDestinations.TRANSACTION_HISTORY_TAB) {
                TransactionHistoryScreen(
                    // Ensure ViewModel is provided correctly if needed by TransactionHistoryScreen
                    // viewModel = viewModel(factory = YourHistoryViewModelFactory(...)),
                    onNavigateBack = { bottomNavHostController.popBackStack() } // Example if it has back navigation
                )
            }
            // Analytics composable route removed
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MerchantAppTheme {
        MainScreen(
            onLogoutRequest = {},
            onNavigateToQrScan = {}
            // onNavigateToTransactionHistory removed from preview call
        )
    }
}
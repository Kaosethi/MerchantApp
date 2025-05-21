package com.example.merchantapp.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.merchantapp.navigation.BottomNavDestinations
import com.example.merchantapp.ui.amount.AmountEntryScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme


// Example data class for Bottom Navigation items
data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogoutRequest: () -> Unit,
    onNavigateToQrScan: (amount: String) -> Unit,
    onNavigateToTransactionHistory: () -> Unit
) {
    val bottomNavHostController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(
            label = "Charge",
            icon = Icons.Filled.Home,
            route = BottomNavDestinations.AMOUNT_ENTRY
        ),
        BottomNavItem(
            label = "History",
            icon = Icons.Filled.History,
            route = BottomNavDestinations.TRANSACTION_HISTORY
        ),
        BottomNavItem(
            label = "Analytics",
            icon = Icons.Filled.Analytics,
            route = BottomNavDestinations.ANALYTICS
        )
    )

    Scaffold(
        topBar = {
            // This TopAppBar is for the MainScreen itself.
            // If AmountEntryScreen is the main content area and also needs a TopAppBar with logout,
            // it would define its own, or this TopAppBar would need to be conditional.
            // For now, assuming logout is primarily from MainScreen's structure.
            TopAppBar(
                title = { Text("Merchant Portal") },
                actions = {
                    IconButton(onClick = onLogoutRequest) { // This logout is for MainScreen's TopAppBar
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavHostController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.route == item.route,
                        onClick = {
                            if (item.route == BottomNavDestinations.TRANSACTION_HISTORY) {
                                onNavigateToTransactionHistory()
                            } else {
                                bottomNavHostController.navigate(item.route) {
                                    popUpTo(bottomNavHostController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavHostController,
            startDestination = BottomNavDestinations.AMOUNT_ENTRY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavDestinations.AMOUNT_ENTRY) {
                AmountEntryScreen(
                    onNavigateToQrScan = onNavigateToQrScan,
                    onLogoutRequest = onLogoutRequest // <<< PASSING onLogoutRequest HERE
                )
            }
            composable(BottomNavDestinations.ANALYTICS) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Analytics Screen (To be implemented)")
                }
            }
            // Note: TransactionHistoryScreen is handled by the main AppNavigation,
            // so it's not a destination within this inner NavHost.
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MerchantAppTheme {
        MainScreen(
            onLogoutRequest = {},
            onNavigateToQrScan = {},
            onNavigateToTransactionHistory = {}
        )
    }
}
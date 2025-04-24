// File: app/src/main/java/com/example/merchantapp/ui/main/MainScreen.kt
package com.example.merchantapp.ui.main // Typo comment can be ignored or fixed

import android.annotation.SuppressLint
// import android.util.Log // REMOVED: Unused import (Line 5)
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt // CHANGED: Import AutoMirrored version
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
// import androidx.compose.material.icons.filled.ListAlt // REMOVED: Deprecated import
import androidx.compose.material3.*
import androidx.compose.runtime.*
// import androidx.compose.runtime.saveable.rememberSaveable // REMOVED: Unused import (Line 13)
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.merchantapp.navigation.BottomNavScreens
import com.example.merchantapp.ui.analytics.AnalyticsDashboardScreen
import com.example.merchantapp.ui.amount.AmountEntryScreen
import com.example.merchantapp.ui.summary.TransactionSummaryScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme

// Data class to represent a bottom navigation item
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
// @OptIn(ExperimentalMaterial3Api::class) // REMOVED: Redundant opt-in (Line 36)
@Composable
fun MainScreen(
    // navController: NavController // Pass the main NavController if needed for logout etc.
) {
    val bottomNavController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(
            label = "Home",
            icon = Icons.Filled.Home,
            route = BottomNavScreens.AMOUNT_ENTRY
        ),
        BottomNavItem(
            label = "Summary",
            icon = Icons.AutoMirrored.Filled.ListAlt, // CHANGED: Use AutoMirrored icon (Line 53)
            route = BottomNavScreens.SUMMARY
        ),
        BottomNavItem(
            label = "Analytics",
            icon = Icons.Filled.Analytics,
            route = BottomNavScreens.ANALYTICS
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreens.AMOUNT_ENTRY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavScreens.AMOUNT_ENTRY) {
                AmountEntryScreen()
            }
            composable(BottomNavScreens.SUMMARY) {
                TransactionSummaryScreen()
            }
            composable(BottomNavScreens.ANALYTICS) {
                AnalyticsDashboardScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = true
            )
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun MainScreenPreview() {
    MerchantAppTheme {
        MainScreen()
    }
}
// File: app/src/main/java/com/example/merchantapp/ui/main/MainScreen.kt
package com.example.merchantapp.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
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


data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    onLogoutRequest: () -> Unit // ADDED: Accept logout request callback
) {
    val bottomNavController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, BottomNavScreens.AMOUNT_ENTRY),
        BottomNavItem("Summary", Icons.AutoMirrored.Filled.ListAlt, BottomNavScreens.SUMMARY),
        BottomNavItem("Analytics", Icons.Filled.Analytics, BottomNavScreens.ANALYTICS)
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
                // MODIFIED: Pass the onLogoutRequest callback down
                AmountEntryScreen(onLogoutRequest = onLogoutRequest)
            }
            composable(BottomNavScreens.SUMMARY) {
                TransactionSummaryScreen() // Add onLogoutRequest here too if needed later
            }
            composable(BottomNavScreens.ANALYTICS) {
                AnalyticsDashboardScreen() // Add onLogoutRequest here too if needed later
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
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
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
        // MODIFIED: Provide dummy lambda for preview
        MainScreen(onLogoutRequest = {})
    }
}
// File: app/src/main/java/com/example/merchantapp/ui/main/MainScreen.kt
package com.example.merchantapp.ui.main

// --- Imports ---
// Use Optimize Imports (Ctrl+Alt+O / Cmd+Option+O)
import android.annotation.SuppressLint
// REMOVED: import android.util.Log (no longer needed here)
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.merchantapp.R
import com.example.merchantapp.navigation.BottomNavScreens
import com.example.merchantapp.ui.analytics.AnalyticsDashboardScreen
import com.example.merchantapp.ui.amount.AmountEntryScreen // Ensure this is imported
import com.example.merchantapp.ui.summary.TransactionSummaryScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme
// --- End Imports ---

data class BottomNavItemData(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    onLogoutRequest: () -> Unit,
    onNavigateToQrScan: (String) -> Unit
) {
    val bottomNavController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItemData(stringResource(R.string.bottom_nav_home), Icons.Filled.Home, BottomNavScreens.AMOUNT_ENTRY),
        BottomNavItemData(stringResource(R.string.bottom_nav_summary), Icons.AutoMirrored.Filled.ListAlt, BottomNavScreens.SUMMARY),
        BottomNavItemData(stringResource(R.string.bottom_nav_analytics), Icons.Filled.Analytics, BottomNavScreens.ANALYTICS)
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        // *** Nested NavHost ***
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreens.AMOUNT_ENTRY,
            modifier = Modifier.padding(innerPadding)
        ) {
            // *** MODIFIED: Amount Entry Route Restored ***
            composable(BottomNavScreens.AMOUNT_ENTRY) {
                // --- RESTORED ACTUAL AmountEntryScreen CALL ---
                AmountEntryScreen(
                    // ViewModel will be implicitly provided by Hilt/viewModel() call inside AmountEntryScreen
                    onLogoutRequest = onLogoutRequest, // Pass down the lambda
                    onNavigateToQrScan = onNavigateToQrScan // Pass down the lambda
                )
                // --- REMOVED Test Text and Log ---
            }
            // Other bottom nav destinations remain the same
            composable(BottomNavScreens.SUMMARY) {
                TransactionSummaryScreen() // Assuming this is a simple placeholder
            }
            composable(BottomNavScreens.ANALYTICS) {
                AnalyticsDashboardScreen() // Assuming this is a simple placeholder
            }
        }
    }
}


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItemData>,
    modifier: Modifier = Modifier
) {
    // ... (BottomNavigationBar implementation remains the same) ...
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
        MainScreen(onLogoutRequest = {}, onNavigateToQrScan = {})
    }
}
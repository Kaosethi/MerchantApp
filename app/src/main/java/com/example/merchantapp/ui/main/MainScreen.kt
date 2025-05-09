// File: app/src/main/java/com/example/merchantapp/ui/main/MainScreen.kt
package com.example.merchantapp.ui.main

// --- Imports ---
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.merchantapp.R
import com.example.merchantapp.navigation.BottomNavScreens
import com.example.merchantapp.ui.analytics.AnalyticsDashboardScreen // Assuming this exists
import com.example.merchantapp.ui.amount.AmountEntryScreen
// MODIFIED: Ensure the correct TransactionSummaryScreen is imported
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
    // Pass topLevelNavController if MainScreen needs to trigger navigations outside its own bottom nav scope
    // topLevelNavController: NavHostController
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
        // You can add a TopAppBar here if MainScreen itself should have one,
        // independent of the content of the bottom nav tabs.
        // topBar = { TopAppBar(title = { Text("Merchant App") }) }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreens.AMOUNT_ENTRY,
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        ) {
            composable(BottomNavScreens.AMOUNT_ENTRY) {
                AmountEntryScreen(
                    onLogoutRequest = onLogoutRequest,
                    onNavigateToQrScan = onNavigateToQrScan
                )
            }
            composable(BottomNavScreens.SUMMARY) {
                // MODIFIED: Call the actual TransactionSummaryScreen
                // The ViewModel will be provided internally by `viewModel()`
                TransactionSummaryScreen()
            }
            composable(BottomNavScreens.ANALYTICS) {
                // TODO: Replace with actual AnalyticsDashboardScreen if it's more complex
                // For now, assuming it's a simple composable or uses its own ViewModel
                AnalyticsDashboardScreen()
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
    NavigationBar(modifier = modifier) { // Using Material 3 NavigationBar
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
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = true // Material 3 often shows labels by default, but explicit is fine
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
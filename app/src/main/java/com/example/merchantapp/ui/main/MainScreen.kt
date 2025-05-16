// MODIFIED: app/src/main/java/com/example/merchantapp/ui/main/MainScreen.kt
package com.example.merchantapp.ui.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.merchantapp.R
import com.example.merchantapp.navigation.BottomNavScreens
import com.example.merchantapp.ui.amount.AmountEntryScreen
import com.example.merchantapp.ui.analytics.AnalyticsDashboardScreen
import com.example.merchantapp.ui.summary.TransactionSummaryScreen
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.MainViewModel

// data class BottomNavItemData remains the same...
data class BottomNavItemData(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    onLogoutRequest: () -> Unit,
    onNavigateToQrScan: (String) -> Unit
) {
    val bottomNavController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        Log.d("MainScreen", "LaunchedEffect: Triggering fetchMerchantProfile")
        mainViewModel.fetchMerchantProfile()
    }

    val bottomNavItems = listOf(
        BottomNavItemData(stringResource(R.string.bottom_nav_home), Icons.Filled.Home, BottomNavScreens.AMOUNT_ENTRY),
        BottomNavItemData(stringResource(R.string.bottom_nav_summary), Icons.AutoMirrored.Filled.ListAlt, BottomNavScreens.SUMMARY),
        BottomNavItemData(stringResource(R.string.bottom_nav_analytics), Icons.Filled.Analytics, BottomNavScreens.ANALYTICS)
    )

    Scaffold(
        topBar = {
            // MODIFIED: Use CenterAlignedTopAppBar for centered title
            CenterAlignedTopAppBar(
                title = {
                    val profile = uiState.merchantProfile
                    if (uiState.isLoadingProfile) {
                        Text("Loading Profile...")
                    } else if (profile != null && profile.businessName != null) {
                        // MODIFIED: Just the business name
                        Text(profile.businessName)
                    } else if (uiState.profileErrorMessage != null) {
                        Text("Profile Error")
                    } else {
                        Text("Merchant Dashboard") // Fallback title
                    }
                }
                // You can add navigationIcon or actions if needed:
                // navigationIcon = { IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Menu, "Menu") } },
                // actions = { IconButton(onClick = { mainViewModel.refreshMerchantProfile() }) { Icon(Icons.Default.Refresh, "Refresh") } }
            )
        },
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
                AmountEntryScreen(
                    onLogoutRequest = onLogoutRequest,
                    onNavigateToQrScan = onNavigateToQrScan
                )
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

// BottomNavigationBar composable remains the same...
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItemData>,
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
        MainScreen(onLogoutRequest = {}, onNavigateToQrScan = {})
    }
}
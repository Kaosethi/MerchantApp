package com.example.merchantapp.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
// import com.valentinilk.shimmer.shimmer // Assuming you might add this later
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.merchantapp.model.ApiTransactionItem
import com.example.merchantapp.model.SettlementItem
import com.example.merchantapp.model.TransactionSummary
import com.example.merchantapp.navigation.BottomNavDestinations // Import your BottomNavDestinations
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// The local MerchantAppDestinations object can be removed from this file
// if BottomNavDestinations serves the purpose for these tab routes.

@SuppressLint("SuspiciousModifierThen")
fun Modifier.shimmerEffect(): Modifier = this.then(alpha(0.5f)) // Basic shimmer placeholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, // This is the bottomNavHostController from MainScreen
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("th", "TH")) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh dashboard")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Merchant Name
            item {
                if (uiState.isLoadingSummary && uiState.merchantName.isBlank()) {
                    MerchantNamePlaceholder()
                } else if (uiState.merchantName.isNotBlank()) {
                    Text(
                        text = uiState.merchantName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Summary Cards
            item {
                SummarySection(
                    uiState = uiState,
                    currencyFormatter = currencyFormatter
                )
            }

            // Shortcut Buttons
            item {
                ShortcutButtons(
                    onNavigateToCreateTransaction = {
                        navController.navigate(BottomNavDestinations.AMOUNT_ENTRY_TAB) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToViewHistory = {
                        navController.navigate(BottomNavDestinations.TRANSACTION_HISTORY_TAB) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Recent Transactions
            item {
                SectionTitleWithViewAll(
                    title = "Recent Transactions",
                    onViewAllClicked = {
                        navController.navigate(BottomNavDestinations.TRANSACTION_HISTORY_TAB) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                RecentTransactionsSection(
                    isLoading = uiState.isLoadingRecentTransactions,
                    transactions = uiState.recentTransactions,
                    currencyFormatter = currencyFormatter,
                    onTransactionClick = { transactionId ->
                        println("Clicked transaction: $transactionId")
                        // Optional: Navigate to transaction detail:
                        // navController.navigate("${YourAppDestinations.TRANSACTION_DETAIL_ROUTE}/$transactionId") // Use main app nav controller if navigating outside bottom bar
                    }
                )
            }

            // Recent Settlements
            item {
                SectionTitle(title = "Recent Settlements")
                RecentSettlementsSection(
                    settlements = uiState.recentSettlements
                )
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = "Error: ${uiState.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun MerchantNamePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(top=8.dp)
            .shimmerEffect()
    )
}


@Composable
fun SummarySection(
    uiState: DashboardScreenState,
    currencyFormatter: NumberFormat
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (uiState.isLoadingSummary) {
            SummaryCardPlaceholder()
            SummaryCardPlaceholder()
            SummaryCardPlaceholder()
        } else {
            uiState.todaySummary?.let {
                SummaryCard(title = "Today", data = it, currencyFormatter)
            }
            uiState.thisWeekSummary?.let {
                SummaryCard(title = "This Week", data = it, currencyFormatter)
            }
            uiState.thisMonthSummary?.let {
                SummaryCard(title = "This Month", data = it, currencyFormatter)
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, data: TransactionSummary, currencyFormatter: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Volume:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currencyFormatter.format(data.totalAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Transactions:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${data.count}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SummaryCardPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .shimmerEffect(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) { /* Empty content for placeholder */ }
}


@Composable
fun ShortcutButtons(
    onNavigateToCreateTransaction: () -> Unit,
    onNavigateToViewHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToCreateTransaction,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) { Text("New Transaction") }
        OutlinedButton(
            onClick = onNavigateToViewHistory,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Full History") }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SectionTitleWithViewAll(
    title: String,
    onViewAllClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onViewAllClicked) { Text("View All") }
    }
}


@Composable
fun RecentTransactionsSection(
    isLoading: Boolean,
    transactions: List<ApiTransactionItem>,
    currencyFormatter: NumberFormat,
    onTransactionClick: (String) -> Unit
) {
    Column {
        if (isLoading) {
            (1..3).forEach { _ -> TransactionRowPlaceholder() }
        } else if (transactions.isEmpty()) {
            Text(
                "No recent transactions found.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            transactions.forEachIndexed { index, transaction ->
                TransactionRow(
                    transaction = transaction,
                    currencyFormatter = currencyFormatter,
                    onClick = { onTransactionClick(transaction.legId) }
                )
                if (index < transactions.size - 1) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: ApiTransactionItem,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT) }
    val transactionDate = transaction.eventTimestamp?.let {
        try { ZonedDateTime.parse(it).format(dateFormatter) } catch (e: Exception) { "Invalid date" }
    } ?: "N/A"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.displayDescription ?: transaction.originalDescription ?: "Transaction",
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = transactionDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = try {
                currencyFormatter.format(transaction.amount.toDouble())
            } catch (e: NumberFormatException) {
                transaction.amount
            },
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = if (transaction.status == "Completed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun TransactionRowPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).shimmerEffect())
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).shimmerEffect())
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.width(60.dp).height(24.dp).shimmerEffect())
    }
}


@Composable
fun RecentSettlementsSection(settlements: List<SettlementItem>) {
    Column {
        if (settlements.isEmpty()) {
            Text(
                "No recent settlements to display.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            settlements.forEachIndexed { index, settlement ->
                SettlementRow(settlement = settlement)
                if (index < settlements.size - 1) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun SettlementRow(settlement: SettlementItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = settlement.periodDescription,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Settled on: ${settlement.settlementDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = settlement.amount,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
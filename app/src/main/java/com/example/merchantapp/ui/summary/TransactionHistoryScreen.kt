package com.example.merchantapp.ui.summary // Or com.example.merchantapp.ui.history

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Changed to itemsIndexed for load more
import androidx.compose.foundation.lazy.rememberLazyListState // For scroll detection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button // For potential "Load More" button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // For Snackbar and scroll events
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf // For checking if scrolled to end
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.model.DeclineReason
import com.example.merchantapp.model.TransactionItem
import com.example.merchantapp.model.TransactionStatus
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.TransactionHistoryViewModel // Renamed ViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ViewModel Factory for AndroidViewModel if not using Hilt
class TransactionHistoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionHistoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for TransactionHistoryViewModelFactory")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen( // Renamed from TransactionSummaryScreen
    viewModel: TransactionHistoryViewModel = viewModel(
        factory = TransactionHistoryViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current // For potential toasts or other context needs

    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault()) }

    // For LazyColumn scroll state and triggering load more
    val listState = rememberLazyListState()
    val reachedBottom: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index == listState.layoutInfo.totalItemsCount - 1 && listState.layoutInfo.totalItemsCount > 0
        }
    }

    // Effect for Snackbar messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            // Optionally clear the error message in VM after showing
            // viewModel.clearErrorMessage()
        }
    }

    // Effect for loading more items when bottom is reached
    LaunchedEffect(reachedBottom) {
        if (reachedBottom && !uiState.isLoading) { // Check isLoading to prevent multiple calls
            viewModel.loadMoreTransactions()
        }
    }


    if (uiState.showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate
                ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.showDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedLocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onDateSelected(selectedLocalDate)
                    } ?: viewModel.onDateSelected(null)
                    viewModel.showDatePicker(false)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDatePicker(false) }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showTransactionDetailDialog && uiState.transactionForDetail != null) {
        TransactionDetailDialog(
            transaction = uiState.transactionForDetail!!,
            onDismiss = { viewModel.showTransactionDetails(null) },
            dateTimeFormatter = dateTimeFormatter
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") }, // Renamed title
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            FilterControls(
                selectedDate = uiState.selectedDate,
                selectedStatus = uiState.selectedStatusFilter,
                statusOptions = uiState.availableStatusFilters, // Make sure VM provides this
                onDateClick = { viewModel.showDatePicker(true) },
                onStatusChange = { viewModel.onStatusFilterChanged(it) },
                onFilterClick = { viewModel.applyFiltersAndFetch() }, // Changed to applyFiltersAndFetch
                onClearDate = { viewModel.onDateSelected(null) }, // Clears date and applies local filter
                dateFormatter = dateFormatter
            )

            // Use filteredTransactions for display
            if (uiState.isLoading && uiState.filteredTransactions.isEmpty()) { // Show main loader only if list is empty
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTransactions.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions found matching your criteria.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    state = listState, // Attach listState
                    modifier = Modifier.weight(1f), // Allow space for bottom loader/button
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.filteredTransactions, key = { _, item -> item.fullTransactionId }) { index, transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onItemClick = { viewModel.showTransactionDetails(it) },
                            dateTimeFormatter = dateTimeFormatter
                        )
                    }
                    // Optional: Show a loading indicator at the bottom while fetching more
                    if (uiState.isLoading && uiState.filteredTransactions.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterControls(
    selectedDate: LocalDate?,
    selectedStatus: String,
    statusOptions: List<String>,
    onDateClick: () -> Unit,
    onStatusChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onClearDate: () -> Unit,
    dateFormatter: DateTimeFormatter
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Adjusted padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = selectedDate?.format(dateFormatter) ?: "Select Date", // Changed placeholder
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, "Select Date") },
                trailingIcon = {
                    if (selectedDate != null) {
                        IconButton(onClick = onClearDate, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Clear, "Clear Date")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1.5f)
                    .clickable(onClick = onDateClick),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Box(modifier = Modifier.weight(1.2f)) { // Use Box for better dropdown width control
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface) // Ensure dropdown bg
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    onStatusChange(status)
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }


            IconButton(
                onClick = onFilterClick, // This now calls applyFiltersAndFetch in VM
                modifier = Modifier.size(48.dp) // Standard IconButton size
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Apply Filters",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionItem,
    onItemClick: (TransactionItem) -> Unit,
    dateTimeFormatter: DateTimeFormatter
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("th", "TH")) }

    val statusColor = when (transaction.status) {
        TransactionStatus.APPROVED -> Color(0xFF2E7D32) // Darker Green
        TransactionStatus.DECLINED -> Color(0xFFC62828) // Darker Red
        TransactionStatus.FAILED -> Color(0xFFD32F2F)   // Darker Red
        TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }
    val amountColor = when (transaction.status) {
        TransactionStatus.APPROVED -> statusColor // Green for approved amounts
        else -> MaterialTheme.colorScheme.onSurface // Default color for other statuses
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(transaction) },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) // Subtle bg
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp) // Adjusted padding
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    // Use transaction.displayDescription if you prefer backend formatted,
                    // or construct one here based on type and parties.
                    // For now, using category and related account.
                    text = "${transaction.category} - ${transaction.accountOwnerName} (${transaction.accountId})",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.dateTime.format(dateTimeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if ((transaction.status == TransactionStatus.DECLINED || transaction.status == TransactionStatus.FAILED) &&
                    transaction.reasonForDecline != null && transaction.reasonForDecline !is DeclineReason.None
                ) {
                    Text(
                        text = "Reason: ${transaction.reasonForDecline.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = currencyFormat.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = transaction.status.name.let { it.first() + it.substring(1).lowercase() }, // Title Case
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), // Slightly smaller
                    color = Color.White,
                    modifier = Modifier
                        .background(statusColor, shape = MaterialTheme.shapes.extraSmall) // Small radius
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}


@Composable
fun TransactionDetailDialog(
    transaction: TransactionItem,
    onDismiss: () -> Unit,
    dateTimeFormatter: DateTimeFormatter
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("th", "TH")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaction Details", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRowDialog("Date & Time:", transaction.dateTime.format(dateTimeFormatter))
                DetailRowDialog("Display ID:", transaction.id)
                DetailRowDialog("Payment Group ID:", transaction.fullTransactionId) // Clarified label
                DetailRowDialog("Related Account:", "${transaction.accountOwnerName} (${transaction.accountId})")
                DetailRowDialog("Amount:", currencyFormat.format(transaction.amount))
                DetailRowDialog("Status:", transaction.status.name.let { it.first() + it.substring(1).lowercase() })
                DetailRowDialog("Category:", transaction.category)
                if ((transaction.status == TransactionStatus.DECLINED || transaction.status == TransactionStatus.FAILED) &&
                    transaction.reasonForDecline != null && transaction.reasonForDecline !is DeclineReason.None
                ) {
                    DetailRowDialog("Reason:", transaction.reasonForDecline.message)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
private fun DetailRowDialog(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(130.dp)) // Increased width for longer labels
        Text(value)
    }
}


// --- Previews ---
// Dummy ViewModel for Previews (using the new ViewModel name and factory pattern for consistency)
class PreviewTransactionHistoryViewModel(application: Application, val darkMode: Boolean = false) : TransactionHistoryViewModel(application) {
    init {
        val mockTransactions = listOf(
            TransactionItem("T006", "FTXN-MOCK-006", LocalDateTime.of(2024,3,25,10,12), "ACC-BEN-001", "Debby Approved", 400.00, TransactionStatus.APPROVED, "Snacks"),
            TransactionItem("T005", "FTXN-MOCK-005", LocalDateTime.of(2024,3,21,12,30), "ACC-BEN-002", "Debby L. Declined", 1500.00, TransactionStatus.DECLINED, "Lunch", DeclineReason.InsufficientBalance),
            TransactionItem("T007", "FTXN-MOCK-007", LocalDateTime.of(2024,3,22,14,0), "ACC-BEN-003", "Chris Failed", 200.00, TransactionStatus.FAILED, "Subscription", DeclineReason.Other("Payment Gateway Error")),
            TransactionItem("T008", "FTXN-MOCK-008", LocalDateTime.of(2024,3,23,15,45), "ACC-BEN-004", "Pat Pending", 75.00, TransactionStatus.PENDING, "Books"),
            TransactionItem("T004", "FTXN-MOCK-004", LocalDateTime.of(2024,3,21,11,0), "ACC-BEN-005", "Alice D. Approved", 550.50, TransactionStatus.APPROVED, "Coffee"),
            TransactionItem("T001", "FTXN-MOCK-001", LocalDateTime.of(2024,3,20,9,0), "ACC-BEN-006", "Bob L. Approved", 1000.00, TransactionStatus.APPROVED, "Parking"),
            TransactionItem("T002", "FTXN-MOCK-002", LocalDateTime.of(2024,3,18,16,30), "ACC-BEN-007", "Alice D. Declined", 2000.00, TransactionStatus.DECLINED, "Electronics", DeclineReason.AccountSuspended),
            TransactionItem("T003", "FTXN-MOCK-003", LocalDateTime.of(2024,3,17,14,15), "ACC-BEN-008", "Charlie M. Failed", 3000.00, TransactionStatus.FAILED, "Movies", DeclineReason.Other("System Error"))
        ).sortedByDescending { it.dateTime }
        super._transactionUiState.value = TransactionSummaryUiState( // Still uses TransactionSummaryUiState name
            isLoading = false,
            allTransactions = mockTransactions,
            filteredTransactions = mockTransactions,
            availableStatusFilters = listOf("All", "Approved", "Declined", "Pending", "Failed")
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TransactionHistoryScreenPreview() { // Renamed Preview
    MerchantAppTheme {
        // For AndroidViewModel in Preview, provide a dummy Application
        val dummyApplication = LocalContext.current.applicationContext as Application
        TransactionHistoryScreen(viewModel = PreviewTransactionHistoryViewModel(dummyApplication))
    }
}
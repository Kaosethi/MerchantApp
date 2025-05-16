// TransactionSummaryScreen.kt
package com.example.merchantapp.ui.summary

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.model.DeclineReason
import com.example.merchantapp.model.TransactionItem
import com.example.merchantapp.model.TransactionStatus
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.TransactionSummaryViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSummaryScreen(
    viewModel: TransactionSummaryViewModel = viewModel(modelClass = TransactionSummaryViewModel::class.java)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault()) }

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
                    } ?: viewModel.onDateSelected(null) // Allow clearing by selecting no date
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
                title = { Text("Transaction Summary") },
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
                statusOptions = uiState.availableStatusFilters,
                onDateClick = { viewModel.showDatePicker(true) },
                onStatusChange = { viewModel.onStatusFilterChanged(it) },
                onFilterClick = { viewModel.applyFilters() },
                onClearDate = { viewModel.onDateSelected(null) },
                dateFormatter = dateFormatter
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTransactions.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions found matching your criteria.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredTransactions, key = { it.fullTransactionId }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onItemClick = { viewModel.showTransactionDetails(it) },
                            dateTimeFormatter = dateTimeFormatter
                        )
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
        shadowElevation = 2.dp // or color = MaterialTheme.colorScheme.surfaceVariant for subtle bg difference
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = selectedDate?.format(dateFormatter) ?: "mm/dd/yyyy",
                onValueChange = {}, // Not directly editable
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

            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {}, // Not directly editable
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
                    onDismissRequest = { statusDropdownExpanded = false }
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

            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(48.dp)
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
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("th", "TH")) } // For Baht ฿

    // MODIFIED: Added FAILED case to make the 'when' exhaustive
    val statusColor = when (transaction.status) {
        TransactionStatus.APPROVED -> Color(0xFF4CAF50) // Green
        TransactionStatus.DECLINED -> Color(0xFFF44336) // Red
        TransactionStatus.FAILED -> Color(0xFFF44336)   // Also Red, or choose another color like Orange
        TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Greyish
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(transaction) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = transaction.dateTime.format(dateTimeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Acct: ${transaction.accountId} (${transaction.accountOwnerName})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tx ID: ${transaction.id}", // Assuming 'id' is the short display ID
                    style = MaterialTheme.typography.bodySmall
                )
                // Show decline reason only if status is DECLINED or FAILED and reason exists
                if ((transaction.status == TransactionStatus.DECLINED || transaction.status == TransactionStatus.FAILED) &&
                    transaction.reasonForDecline != DeclineReason.None &&
                    transaction.reasonForDecline?.message?.isNotEmpty() == true
                ) {
                    Text(
                        text = "Reason: ${transaction.reasonForDecline?.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor, // Use status color for reason text
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = currencyFormat.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    // Color amount based on status for more visual feedback
                    color = if (transaction.status == TransactionStatus.APPROVED) statusColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = transaction.status.name.uppercase(Locale.getDefault()), // Uppercase for consistency
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = Color.White, // Text color on badge
                    modifier = Modifier
                        .background(statusColor, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("th", "TH")) } // For Baht ฿

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaction Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRowDialog("ID:", transaction.id) // Short display ID
                DetailRowDialog("Full ID:", transaction.fullTransactionId) // Added full ID
                DetailRowDialog("Timestamp:", transaction.dateTime.format(dateTimeFormatter))
                DetailRowDialog("Account:", "${transaction.accountId} (${transaction.accountOwnerName})")
                DetailRowDialog("Amount:", currencyFormat.format(transaction.amount))
                DetailRowDialog("Status:", transaction.status.name)
                DetailRowDialog("Category:", transaction.category)
                if ((transaction.status == TransactionStatus.DECLINED || transaction.status == TransactionStatus.FAILED) &&
                    transaction.reasonForDecline != DeclineReason.None &&
                    transaction.reasonForDecline?.message?.isNotEmpty() == true
                ) {
                    DetailRowDialog("Reason:", transaction.reasonForDecline?.message ?: "N/A")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun DetailRowDialog(label: String, value: String) {
    Row {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.widthIn(min = 80.dp, max = 120.dp)) // Ensure enough width for "Timestamp:"
        Text(value)
    }
}

// --- Previews ---
@Preview(showBackground = true, widthDp = 380)
@Composable
fun TransactionSummaryScreenPreview() {
    MerchantAppTheme {
        TransactionSummaryScreen(viewModel = PreviewTransactionSummaryViewModel())
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Summary Screen Dark")
@Composable
fun TransactionSummaryScreenDarkPreview() {
    MerchantAppTheme(darkTheme = true) {
        TransactionSummaryScreen(viewModel = PreviewTransactionSummaryViewModel(darkMode = true))
    }
}

@Preview(showBackground = true, name = "Transaction Row Approved")
@Composable
fun TransactionRowApprovedPreview() {
    MerchantAppTheme {
        TransactionRow(
            transaction = TransactionItem("T001", "FTXN001", LocalDateTime.now(), "ACC-001", "John Doe", 1200.50, TransactionStatus.APPROVED, "Groceries"),
            onItemClick = {},
            dateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a")
        )
    }
}

@Preview(showBackground = true, name = "Transaction Row Declined")
@Composable
fun TransactionRowDeclinedPreview() {
    MerchantAppTheme {
        TransactionRow(
            transaction = TransactionItem("T002", "FTXN002", LocalDateTime.now().minusDays(1), "ACC-002", "Jane Smith", 750.00, TransactionStatus.DECLINED, "Utilities", DeclineReason.InsufficientBalance),
            onItemClick = {},
            dateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a")
        )
    }
}

@Preview(showBackground = true, name = "Transaction Row Failed")
@Composable
fun TransactionRowFailedPreview() {
    MerchantAppTheme {
        TransactionRow(
            transaction = TransactionItem("T007", "FTXN007", LocalDateTime.now().minusHours(2), "ACC-007", "Error Case", 100.00, TransactionStatus.FAILED, "Services", DeclineReason.Other("Network Timeout")),
            onItemClick = {},
            dateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a")
        )
    }
}

@Preview(showBackground = true, name = "Transaction Row Pending")
@Composable
fun TransactionRowPendingPreview() {
    MerchantAppTheme {
        TransactionRow(
            transaction = TransactionItem("T008", "FTXN008", LocalDateTime.now().minusMinutes(30), "ACC-008", "Pending User", 250.00, TransactionStatus.PENDING, "Online Order"),
            onItemClick = {},
            dateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a")
        )
    }
}


// Dummy ViewModel for Previews
class PreviewTransactionSummaryViewModel(val darkMode: Boolean = false) : TransactionSummaryViewModel() {
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
        ).sortedByDescending { it.dateTime } // Ensure sorted for preview consistency
        super._uiState.value = TransactionSummaryUiState(
            isLoading = false,
            allTransactions = mockTransactions,
            filteredTransactions = mockTransactions,
            availableStatusFilters = listOf("All", "Approved", "Declined", "Pending", "Failed") // Added Failed
        )
    }
}
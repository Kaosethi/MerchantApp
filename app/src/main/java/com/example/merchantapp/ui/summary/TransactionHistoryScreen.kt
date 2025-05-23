package com.example.merchantapp.ui.summary

import android.app.Application
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.model.DeclineReason
import com.example.merchantapp.model.TransactionItem
import com.example.merchantapp.model.TransactionStatus
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.TransactionHistoryViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel = viewModel(
        factory = TransactionHistoryViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM/dd/yy", Locale.getDefault()) }
    val listState = rememberLazyListState()

    val reachedBottom: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index == listState.layoutInfo.totalItemsCount - 1 && listState.layoutInfo.totalItemsCount > 0
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom && !uiState.isLoading && uiState.errorMessage == null) {
            viewModel.loadMoreTransactions()
        }
    }

    if (uiState.showDatePickerDialog) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = uiState.selectedStartDate
                ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            initialSelectedEndDateMillis = uiState.selectedEndDate
                ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            initialDisplayMode = DisplayMode.Picker
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.showDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    val startDateMillis = dateRangePickerState.selectedStartDateMillis
                    val endDateMillis = dateRangePickerState.selectedEndDateMillis
                    val selectedStartLocalDate = startDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    val selectedEndLocalDate = endDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    viewModel.onDateRangeSelected(selectedStartLocalDate, selectedEndLocalDate)
                    viewModel.showDatePicker(false)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDatePicker(false) }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.padding(top=8.dp),
                title = {
                    Text(text = "Select Date Range", modifier = Modifier.padding(start = 24.dp, end=12.dp, top = 16.dp, bottom = 12.dp))
                },
                headline = {
                    val start = dateRangePickerState.selectedStartDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
                    } ?: "Start Date"
                    val end = dateRangePickerState.selectedEndDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
                    } ?: "End Date"
                    Text(text = "$start - $end", modifier = Modifier.padding(start = 24.dp, end=12.dp, top = 0.dp, bottom = 12.dp))
                },
                showModeToggle = true
            )
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
                title = { Text("Transaction History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            FilterControls(
                selectedStartDate = uiState.selectedStartDate,
                selectedEndDate = uiState.selectedEndDate,
                selectedStatus = uiState.selectedStatusFilter,
                statusOptions = uiState.availableStatusFilters.filter { it == "Approved" || it == "Declined" },
                onDateClick = {
                    Log.d("TransactionHistoryScreen", "onDateClick called. Calling viewModel.showDatePicker(true)")
                    viewModel.showDatePicker(true)
                },
                onStatusChange = { viewModel.onStatusFilterChanged(it) },
                onClearAllFilters = { viewModel.clearAllFiltersAndFetch() }, // Correctly calls VM function
                onClearDateRange = { viewModel.onDateRangeSelected(null, null) },
                dateFormatter = dateFormatter
            )

            val currentErrorMessage = uiState.errorMessage
            if (currentErrorMessage != null && uiState.filteredTransactions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Filled.Warning, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = currentErrorMessage, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.refreshTransactions() }) { Text("Retry") }
                }
            } else if (uiState.isLoading && uiState.filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.filteredTransactions.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions found matching your criteria.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(uiState.filteredTransactions, key = { _, item -> item.fullTransactionId }) { _, transaction ->
                        TransactionRow(transaction = transaction, onItemClick = { viewModel.showTransactionDetails(it) }, dateTimeFormatter = dateTimeFormatter)
                    }
                    if (uiState.isLoading && uiState.filteredTransactions.isNotEmpty()) {
                        item { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) } }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterControls(
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    selectedStatus: String,
    statusOptions: List<String>,
    onDateClick: () -> Unit,
    onStatusChange: (String) -> Unit,
    onClearAllFilters: () -> Unit, // Parameter name updated to match usage
    onClearDateRange: () -> Unit,
    dateFormatter: DateTimeFormatter
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    val dateDisplayValue = when {
        selectedStartDate != null && selectedEndDate != null -> "${selectedStartDate.format(dateFormatter)} - ${selectedEndDate.format(dateFormatter)}"
        selectedStartDate != null -> "${selectedStartDate.format(dateFormatter)} - End Date"
        selectedEndDate != null -> "Start Date - ${selectedEndDate.format(dateFormatter)}"
        else -> "Select Date Range"
    }
    val controlMinHeight = 56.dp

    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClickableDateDisplayField(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                dateDisplayValue = dateDisplayValue,
                hasSelection = selectedStartDate != null || selectedEndDate != null,
                onClick = onDateClick,
                onClear = onClearDateRange
            )
            Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                ExposedDropdownMenuBox(expanded = statusDropdownExpanded, onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }, modifier = Modifier.fillMaxHeight()) {
                    OutlinedTextField(
                        value = selectedStatus, onValueChange = {}, readOnly = true, label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().fillMaxHeight(),
                        singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
                    )
                    ExposedDropdownMenu(expanded = statusDropdownExpanded, onDismissRequest = { statusDropdownExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(text = { Text(status) }, onClick = { onStatusChange(status); statusDropdownExpanded = false })
                        }
                    }
                }
            }
            IconButton(
                onClick = onClearAllFilters, // Uses the corrected parameter name
                modifier = Modifier.size(controlMinHeight)
            ) {
                Icon(imageVector = Icons.Filled.ClearAll, contentDescription = "Clear All Filters", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableDateDisplayField(
    modifier: Modifier = Modifier,
    dateDisplayValue: String,
    hasSelection: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
    label: String = "Date Range"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline
    val unfocusedLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    val focusedLabelColor: Color = MaterialTheme.colorScheme.primary
    val defaultShape: Shape = MaterialTheme.shapes.extraSmall
    val minHeight: Dp = 56.dp

    val isPlaceholder = dateDisplayValue == "Select Date Range" || dateDisplayValue.contains("Start Date") || dateDisplayValue.contains("End Date")

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .border(BorderStroke(width = 1.dp, color = unfocusedBorderColor), shape = defaultShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = label,
            color = if (isPlaceholder && !hasSelection) unfocusedLabelColor else focusedLabelColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp, start = 4.dp).background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth().padding(top = if (isPlaceholder && !hasSelection) 0.dp else 8.dp)
        ) {
            Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = "Select Date Range Icon", modifier = Modifier.size(24.dp), tint = LocalContentColor.current.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = dateDisplayValue, style = LocalTextStyle.current.copy(fontSize = 14.sp), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (isPlaceholder && !hasSelection) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
            if (hasSelection) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) { Icon(Icons.Filled.Clear, "Clear Date Range") }
            } else {
                Spacer(modifier = Modifier.size(24.dp))
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
        TransactionStatus.APPROVED -> Color(0xFF2E7D32); TransactionStatus.DECLINED -> Color(0xFFC62828); TransactionStatus.FAILED -> Color(0xFFD32F2F); TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }
    val amountColor = when (transaction.status) { TransactionStatus.APPROVED -> statusColor; else -> MaterialTheme.colorScheme.onSurface }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onItemClick(transaction) },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = "${transaction.category} - ${transaction.accountOwnerName} (${transaction.accountId})", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = transaction.dateTime.format(dateTimeFormatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if ((transaction.status == TransactionStatus.DECLINED || transaction.status == TransactionStatus.FAILED) && transaction.reasonForDecline != null && transaction.reasonForDecline !is DeclineReason.None) {
                    Text(text = "Reason: ${transaction.reasonForDecline.message}", style = MaterialTheme.typography.bodySmall, color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = currencyFormat.format(transaction.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = amountColor)
                Text(text = transaction.status.name.let { it.first() + it.substring(1).lowercase() }, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color.White, modifier = Modifier.background(statusColor, shape = MaterialTheme.shapes.extraSmall).padding(horizontal = 6.dp, vertical = 3.dp))
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
        onDismissRequest = onDismiss, title = { Text("Transaction Details", style = MaterialTheme.typography.headlineSmall) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRowDialog("Date & Time:", transaction.dateTime.format(dateTimeFormatter)); DetailRowDialog("Display ID:", transaction.id); DetailRowDialog("Payment Group ID:", transaction.fullTransactionId)
            DetailRowDialog("Related Account:", "${transaction.accountOwnerName} (${transaction.accountId})"); DetailRowDialog("Amount:", currencyFormat.format(transaction.amount))
            DetailRowDialog("Status:", transaction.status.name.let { it.first() + it.substring(1).lowercase() }); DetailRowDialog("Category:", transaction.category)
            if ((transaction.status == TransactionStatus.DECLINED || transaction.status == TransactionStatus.FAILED) && transaction.reasonForDecline != null && transaction.reasonForDecline !is DeclineReason.None) { DetailRowDialog("Reason:", transaction.reasonForDecline.message) }
        }},
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
private fun DetailRowDialog(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(130.dp)); Text(value)
    }
}

class PreviewTransactionHistoryViewModel(application: Application, private val darkModeUnused: Boolean = false) : TransactionHistoryViewModel(application) {
    init {
        val mockTransactions = listOf(
            TransactionItem("T006", "FTXN-MOCK-006", LocalDateTime.of(2024,3,25,10,12), "ACC-BEN-001", "Debby Approved", 400.00, TransactionStatus.APPROVED, "Snacks"),
            TransactionItem("T005", "FTXN-MOCK-005", LocalDateTime.of(2024,3,21,12,30), "ACC-BEN-002", "Debby L. Declined", 1500.00, TransactionStatus.DECLINED, "Lunch", DeclineReason.InsufficientBalance)
        ).sortedByDescending { it.dateTime }
        _transactionUiState.value = TransactionSummaryUiState(
            isLoading = false, allTransactions = mockTransactions, filteredTransactions = mockTransactions,
            availableStatusFilters = listOf("All", "Approved", "Pending", "Declined", "Failed"),
            selectedStatusFilter = "All", selectedStartDate = null, selectedEndDate = null,
            showDatePickerDialog = false, transactionForDetail = null, showTransactionDetailDialog = false, errorMessage = null
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TransactionHistoryScreenPreview() {
    MerchantAppTheme {
        val dummyApplication = LocalContext.current.applicationContext as Application
        TransactionHistoryScreen(viewModel = PreviewTransactionHistoryViewModel(dummyApplication), onNavigateBack = {})
    }
}
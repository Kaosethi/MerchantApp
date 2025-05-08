// TransactionSuccessScreen.kt
package com.example.merchantapp.ui.transactionsuccess

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
// import androidx.compose.material.icons.filled.Done // Alternative icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.TransactionSuccessViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSuccessScreen(
    viewModel: TransactionSuccessViewModel = viewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val formattedAmount = remember(uiState.amount) {
        try {
            val amountValue = uiState.amount.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale("en", "US")).format(amountValue)
        } catch (e: Exception) {
            uiState.amount // Fallback to raw amount if formatting fails
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Successful") },
                // No navigation icon as this is usually a terminal screen in the flow
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.primary, // Or a specific success color e.g., Color.Green
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Payment Successful!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TransactionDetailItem("Transaction ID:", uiState.transactionId)
                    TransactionDetailItem("Amount:", formattedAmount, isAmount = true)
                    TransactionDetailItem("To:", uiState.beneficiaryName)
                    TransactionDetailItem("Beneficiary Account:", uiState.beneficiaryId)
                    TransactionDetailItem("Description:", uiState.category)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun TransactionDetailItem(label: String, value: String, isAmount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f) // Adjust weight as needed
        )
        Text(
            text = value,
            style = if (isAmount) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f) // Adjust weight as needed
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionSuccessScreenPreview() {
    MerchantAppTheme {
        // Create a dummy SavedStateHandle with preview data
        val previewState = mapOf(
            "amount" to "75.50",
            "beneficiaryId" to "BEN-PREVIEW-001",
            "beneficiaryName" to "Jane Doe Preview",
            "category" to "Shopping",
            "transactionId" to "TXN-PREVIEW-12345"
        )
        val savedStateHandle = SavedStateHandle(previewState)
        val viewModel = TransactionSuccessViewModel(savedStateHandle)

        TransactionSuccessScreen(
            viewModel = viewModel,
            onNavigateToHome = {}
        )
    }
}

@Preview(showBackground = true, name = "Transaction Success Dark Theme")
@Composable
fun TransactionSuccessScreenDarkPreview() {
    MerchantAppTheme(darkTheme = true) {
        val previewState = mapOf(
            "amount" to "120.00",
            "beneficiaryId" to "BEN-DARK-007",
            "beneficiaryName" to "Dark Mode User",
            "category" to "Entertainment",
            "transactionId" to "TXN-DARK-67890"
        )
        val savedStateHandle = SavedStateHandle(previewState)
        val viewModel = TransactionSuccessViewModel(savedStateHandle)

        TransactionSuccessScreen(
            viewModel = viewModel,
            onNavigateToHome = {}
        )
    }
}
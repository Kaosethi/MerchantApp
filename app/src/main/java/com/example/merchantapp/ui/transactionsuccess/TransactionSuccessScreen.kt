package com.example.merchantapp.ui.transactionsuccess

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // For currency formatter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat // For currency
import java.util.Locale       // For currency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSuccessScreen(
    // --- ADD THESE PARAMETERS ---
    transactionId: String,
    amount: String,
    beneficiaryName: String,
    category: String,
    beneficiaryAccountId: String, // This is the Payer's Display ID
    // --- END ADDED PARAMETERS ---
    onNavigateToHome: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("th", "TH")) }
    val formattedAmount = remember(amount) {
        try {
            currencyFormatter.format(amount.toDoubleOrNull() ?: 0.0)
        } catch (e: Exception) {
            amount // fallback
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transaction Successful") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50), // Green color for success
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Payment Successful!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow("Transaction ID:", transactionId)
                    DetailRow("Amount:", formattedAmount)
                    DetailRow("To:", beneficiaryName)
                    DetailRow("Beneficiary Account:", beneficiaryAccountId) // Display Payer's Account ID
                    DetailRow("Description:", category)
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Done")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) { // Copied from PinEntryScreen for consistency
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically // Can be Top for multi-line values
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End // Align value to the end
        )
    }
}

// Add a Preview
// @Preview(showBackground = true)
// @Composable
// fun TransactionSuccessScreenPreview() {
//    MerchantAppTheme {
//        TransactionSuccessScreen(
//            transactionId = "12345-ABCDE-67890",
//            amount = "50.00",
//            beneficiaryName = "Test Child Name",
//            category = "Groceries",
//            beneficiaryAccountId = "STC-2025-XYZ",
//            onNavigateToHome = {}
//        )
//    }
// }
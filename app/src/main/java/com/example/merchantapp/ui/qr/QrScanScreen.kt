// File: app/src/main/java/com/example/merchantapp/ui/qr/QrScanScreen.kt
package com.example.merchantapp.ui.qr

import android.annotation.SuppressLint
// --- Minimal Necessary Imports ---
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.merchantapp.R
import com.example.merchantapp.ui.theme.MerchantAppTheme
import java.text.NumberFormat
import java.util.Locale
// --- End Minimal Imports ---

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QrScanScreen(
    amount: String,
    onNavigateBack: () -> Unit,
    onSimulatedScan: (amount: String, token: String) -> Unit
) {
    val hardcodedTestToken = "TEST-TOKEN-12345"

    // FIX 1: Ensure catch block returns a value for remember
    val formattedAmount = remember(amount) {
        try {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale("en", "US")).format(amountValue)
        } catch (e: Exception) {
            "Error" // Explicitly return "Error" in catch block
        }
    }

    Scaffold(
        // FIX 2: Ensure TopAppBar definition is correct
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_qr_scan)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Amount)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.qr_label_transaction_amount),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.displaySmall
                )
            }

            // Middle Section (Placeholder and Instructions)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.qr_placeholder_scanner_view),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.qr_instructions),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Bottom Section (Simulation Button)
            Button(
                onClick = {
                    onSimulatedScan(amount, hardcodedTestToken)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Simulate Scan Success") // TODO: Add to strings.xml
            }
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun QrScanScreenPreview() {
    MerchantAppTheme {
        QrScanScreen(
            amount = "55.00",
            onNavigateBack = {},
            onSimulatedScan = { _, _ -> }
        )
    }
}
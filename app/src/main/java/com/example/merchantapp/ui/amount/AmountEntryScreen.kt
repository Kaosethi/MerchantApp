// File: app/src/main/java/com/example/merchantapp/ui/amount/AmountEntryScreen.kt
package com.example.merchantapp.ui.amount

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
// REMOVED: text package imports (KeyboardOptions, KeyboardActions)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace // Import Backspace
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// REMOVED: platform imports (LocalFocusManager, LocalSoftwareKeyboardController)
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.merchantapp.R
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.AmountEntryUiState
import com.example.merchantapp.viewmodel.AmountEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AmountEntryScreen(
    viewModel: AmountEntryViewModel = viewModel(),
    onLogoutRequest: () -> Unit,
    onNavigateToQrScan: (String) -> Unit // Takes the raw amount string
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Function to trigger navigation using raw amount from viewmodel state
    val proceedToScan: () -> Unit = {
        if (uiState.isAmountValid) {
            val rawAmount = viewModel.getRawAmountValue() // Get raw value
            Log.d("AmountEntryScreen", "Proceeding to QR scan with amount: $rawAmount")
            onNavigateToQrScan(rawAmount)
            // viewModel.clearAmount() // Optional clear
        } else {
            Log.w("AmountEntryScreen", "Proceed button clicked but amount is invalid: ${uiState.amount}")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.title_amount_entry)) },
                actions = {
                    IconButton(onClick = onLogoutRequest) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.action_logout))
                    }
                }
            )
        }
    ) { innerPadding ->
        AmountNumpadContent( // Renamed content composable
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onDigitClick = viewModel::onDigitClick,
            onDecimalClick = viewModel::onDecimalClick,
            onBackspaceClick = viewModel::onBackspaceClick,
            onProceedClick = proceedToScan
        )
    }
}

// --- MODIFIED Stateless Composable ---
@Composable
fun AmountNumpadContent( // Renamed
    modifier: Modifier = Modifier,
    uiState: AmountEntryUiState,
    onDigitClick: (Char) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onProceedClick: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp), // Adjust padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Amount Display Area ---
        Spacer(Modifier.weight(1f)) // Pushes amount display down a bit
        Text(
            text = uiState.displayAmount, // Display the formatted amount
            style = MaterialTheme.typography.displayLarge, // Make it large
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )
        Spacer(Modifier.weight(1f)) // Pushes numpad down

        // --- Numpad ---
        AmountNumpad(
            onDigitClick = onDigitClick,
            onDecimalClick = onDecimalClick,
            onBackspaceClick = onBackspaceClick,
            // Enable/disable based on state
            isDecimalEnabled = !uiState.amount.contains('.'),
            isBackspaceEnabled = uiState.amount.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Proceed Button ---
        Button(
            onClick = onProceedClick,
            enabled = uiState.isAmountValid,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Proceed to Scan QR")
        }
    }
}

// --- NEW: Numpad Composable ---
@Composable
fun AmountNumpad(
    modifier: Modifier = Modifier,
    onDigitClick: (Char) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    isDecimalEnabled: Boolean,
    isBackspaceEnabled: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // Space between rows
    ) {
        // Rows 1-3 (Digits 1-9)
        listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9')
        ).forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                rowDigits.forEach { digit ->
                    NumpadButton(text = digit.toString()) { onDigitClick(digit) }
                }
            }
        }
        // Row 4 (Decimal, 0, Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use text for decimal point button
            NumpadButton(text = ".", enabled = isDecimalEnabled, onClick = onDecimalClick)
            NumpadButton(text = "0") { onDigitClick('0') }
            // Use Icon for backspace button
            NumpadButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.pin_entry_backspace), // Reuse string
                enabled = isBackspaceEnabled,
                onClick = onBackspaceClick
            )
        }
    }
}

// --- NEW: Reusable Numpad Button ---
@Composable
fun RowScope.NumpadButton( // Use RowScope for weighting
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    // Use TextButton for a flatter look, or OutlinedButton/Button
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium, // Or CircleShape
        modifier = modifier
            .weight(1f) // Equal weight for buttons in a row
            .aspectRatio(1.8f), // Adjust aspect ratio for button shape
        contentPadding = PaddingValues(0.dp)
        // Add colors or elevation if desired
    ) {
        if (text != null) {
            Text(text = text, fontSize = 22.sp) // Adjust font size as needed
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp) // Adjust icon size
            )
        }
    }
}


// --- Previews ---
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun AmountNumpadScreenPreview() { // Renamed Preview
    MerchantAppTheme {
        AmountNumpadContent(
            uiState = AmountEntryUiState(amount = "123.4", displayAmount = "$123.40", isAmountValid = true),
            onDigitClick = {},
            onDecimalClick = {},
            onBackspaceClick = {},
            onProceedClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AmountNumpadPreview() {
    MerchantAppTheme {
        Surface {
            AmountNumpad(
                onDigitClick = {},
                onDecimalClick = {},
                onBackspaceClick = {},
                isDecimalEnabled = false, // Example state
                isBackspaceEnabled = true // Example state
            )
        }
    }
}
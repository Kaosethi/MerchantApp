// File: app/src/main/java/com/example/merchantapp/ui/amount/AmountEntryScreen.kt
package com.example.merchantapp.ui.amount

// --- Imports ---
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Keep explicit imports
// Removed: import androidx.compose.runtime.setValue (not directly used)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
// --- End Imports ---

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AmountEntryScreen(
    viewModel: AmountEntryViewModel = viewModel(),
    onLogoutRequest: () -> Unit,
    onNavigateToQrScan: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val proceedToScan: () -> Unit = {
        if (uiState.isAmountValid) {
            val rawAmount = viewModel.getRawAmountValue()
            Log.d("AmountEntryScreen", "Proceeding to QR scan with amount: $rawAmount")
            onNavigateToQrScan(rawAmount)
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
        AmountNumpadContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onDigitClick = { digit -> viewModel.onDigitClick(digit) }, // Use Lambda
            onDecimalClick = { viewModel.onDecimalClick() }, // Use Lambda
            onBackspaceClick = { viewModel.onBackspaceClick() }, // Use Lambda
            onProceedClick = proceedToScan
        )
    }
}

@Composable
fun AmountNumpadContent(
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
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            text = uiState.displayAmount, // Use displayAmount from state
            style = MaterialTheme.typography.displayLarge, // Reverted to displayLarge
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )
        Spacer(Modifier.weight(1f))

        AmountNumpad(
            onDigitClick = onDigitClick,
            onDecimalClick = onDecimalClick,
            onBackspaceClick = onBackspaceClick,
            isDecimalEnabled = !uiState.amount.contains('.'),
            isBackspaceEnabled = uiState.amount.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onProceedClick,
            enabled = uiState.isAmountValid,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Proceed to Scan QR")
        }
    }
}

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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                    NumpadButton(text = digit.toString(), onClick = { onDigitClick(digit) })
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumpadButton(text = ".", enabled = isDecimalEnabled, onClick = onDecimalClick)
            NumpadButton(text = "0", onClick = { onDigitClick('0') })
            NumpadButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.pin_entry_backspace), // Check strings.xml
                enabled = isBackspaceEnabled,
                onClick = onBackspaceClick
            )
        }
    }
}

@Composable
fun RowScope.NumpadButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .weight(1f)
            .aspectRatio(1.8f),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (text != null) {
            Text(text = text, fontSize = 22.sp)
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// --- Previews ---
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun AmountNumpadScreenPreview() {
    MerchantAppTheme {
        AmountNumpadContent(
            uiState = AmountEntryUiState(amount = "123.4", displayAmount = "$123.40", isAmountValid = true),
            onDigitClick = {}, onDecimalClick = {}, onBackspaceClick = {}, onProceedClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AmountNumpadPreview() {
    MerchantAppTheme {
        Surface {
            AmountNumpad(
                onDigitClick = {}, onDecimalClick = {}, onBackspaceClick = {},
                isDecimalEnabled = false, isBackspaceEnabled = true
            )
        }
    }
}
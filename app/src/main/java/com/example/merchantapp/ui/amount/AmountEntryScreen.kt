// File path: app/src/main/java/com/example/merchantapp/ui/amount/AmountEntryScreen.kt
package com.example.merchantapp.ui.amount

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.AmountEntryViewModel
import com.example.merchantapp.viewmodel.AmountEntryUiState // Keep state import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountEntryScreen(
    modifier: Modifier = Modifier,
    viewModel: AmountEntryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formattedAmount by viewModel.formattedAmount.collectAsStateWithLifecycle()
    val isNextEnabled by viewModel.isNextEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.inputError) {
        uiState.inputError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearInputError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Food Market",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = { Spacer(Modifier.width(48.dp)) },
                actions = {
                    IconButton(onClick = {
                        Log.d("AmountEntryScreen", "Logout clicked")
                        Toast.makeText(context, "Logout Action", Toast.LENGTH_SHORT).show()
                        // TODO: Implement actual logout
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout"
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Transaction Amount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formattedAmount,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp)) // Space before keypad

            // --- Keypad ---
            // MODIFIED: Give Keypad weight to make it expand vertically
            Keypad(
                onDigitClick = viewModel::onDigitPress,
                onDecimalClick = viewModel::onDecimalPress,
                onDeleteClick = viewModel::onDeletePress,
                modifier = Modifier
                    .weight(1f) // <<< This makes the Keypad area expand
                    .padding(horizontal = 8.dp, vertical = 8.dp) // Add padding around keypad
            )

            // --- Next Button ---
            // REMOVED: Spacer(modifier = Modifier.weight(1f)) was here

            Button(
                onClick = {
                    val amountValue = viewModel.getCurrentAmountValue()
                    Log.d("AmountEntryScreen", "Next Clicked. Amount Value: $amountValue")
                    viewModel.onNextClick()
                    // TODO: Implement navigation to QR Scan Screen
                    Toast.makeText(context, "Proceeding with $formattedAmount", Toast.LENGTH_SHORT).show()
                },
                enabled = isNextEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) // Padding at screen bottom
                    .height(52.dp) // Give button a slightly larger fixed height
            ) {
                Text("Next", fontSize = 16.sp) // Slightly larger text
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

// --- Keypad Composable ---
@Composable
fun Keypad(
    onDigitClick: (Char) -> Unit,
    onDecimalClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier // Modifier now includes weight(1f) from caller
) {
    // MODIFIED: Column fills height provided by weight, distributes rows evenly
    Column(
        modifier = modifier.fillMaxHeight(), // Fill the height given by the weight
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly // Distribute rows evenly
    ) {
        val keypadLayout = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf('.', '0', 'D')
        )

        keypadLayout.forEach { row ->
            // MODIFIED: Row fills width and takes equal vertical weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Each row takes equal vertical space
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally) // Horizontal spacing
            ) {
                row.forEach { key ->
                    // MODIFIED: Button fills height within its row
                    KeypadButton(
                        key = key,
                        onClick = {
                            when (key) {
                                '.' -> onDecimalClick()
                                'D' -> onDeleteClick()
                                else -> onDigitClick(key)
                            }
                        },
                        modifier = Modifier
                            .fillMaxHeight() // Button fills row height
                            .weight(1f)      // Buttons share horizontal space in row
                    )
                }
            }
        }
    }
}

// --- Individual Keypad Button ---
@Composable
fun KeypadButton(
    key: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Modifier now includes fillMaxHeight & weight(1f)
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier // Use the passed modifier
            // .aspectRatio(1.6f) // REMOVED: Aspect ratio conflicts with fillMaxHeight/weight
            .padding(4.dp), // Add small padding around each button
        shape = MaterialTheme.shapes.medium, // Medium rounding
        // Adjust content padding for larger buttons if needed
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (key == 'D') {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                modifier = Modifier.size(30.dp) // Slightly larger icon
            )
        } else {
            Text(
                text = key.toString(),
                fontSize = 28.sp // Larger font size for buttons
            )
        }
    }
}


// --- Preview Function for the entire screen ---
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun AmountEntryScreenPreview() {
    MerchantAppTheme {
        AmountEntryScreen()
    }
}

// --- Preview for the Keypad Component ---
@Preview(showBackground = true)
@Composable
fun KeypadPreview() {
    MerchantAppTheme {
        // Give the keypad some height in preview to see layout
        Box(modifier = Modifier.height(300.dp)) {
            Keypad(onDigitClick = {}, onDecimalClick = {}, onDeleteClick = {})
        }
    }
}
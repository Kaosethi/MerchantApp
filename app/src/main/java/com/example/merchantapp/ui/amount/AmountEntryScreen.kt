// File path: app/src/main/java/com/example/merchantapp/ui/amount/AmountEntryScreen.kt
package com.example.merchantapp.ui.amount

// import android.util.Log // You might need this again
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Logout // ADDED BACK: Logout Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // ADDED BACK: For string resources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.R // ADDED BACK: R class import
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.AmountEntryViewModel
import com.example.merchantapp.viewmodel.AmountEntryUiState

@OptIn(ExperimentalMaterial3Api::class) // ADDED BACK: For Scaffold/TopAppBar
@Composable
fun AmountEntryScreen(
    modifier: Modifier = Modifier, // Modifier from MainScreen (includes padding)
    viewModel: AmountEntryViewModel = viewModel(),
    onLogoutRequest: () -> Unit // ADDED: Callback for logout button press
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

    // ADDED BACK: Scaffold wrapper
    Scaffold(
        // ADDED BACK: TopAppBar definition
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        // TODO: Replace "Food Market" with actual Merchant Name later
                        "Food Market",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                // Provides centering for title when actions are present
                navigationIcon = { Spacer(Modifier.width(48.dp)) }, // Placeholder for centering
                actions = {
                    IconButton(onClick = onLogoutRequest) { // MODIFIED: Call the callback
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.action_logout) // Use string resource
                        )
                    }
                },
                // Optional: Add colors if needed
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Or primaryContainer etc.
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant // Or onPrimaryContainer etc.
                )
            )
        }
    ) { paddingValues -> // Padding values from Scaffold (includes top bar)
        Column(
            // MODIFIED: Apply modifier *and* padding from Scaffold
            modifier = modifier // Apply padding from MainScreen's NavHost
                .padding(paddingValues) // Apply padding from this screen's Scaffold
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Add horizontal padding for content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // REMOVED: Spacer at the top (TopAppBar provides space now)
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
            Keypad(
                onDigitClick = viewModel::onDigitPress,
                onDecimalClick = viewModel::onDecimalPress,
                onDeleteClick = viewModel::onDeletePress,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )

            // --- Next Button ---
            Button(
                onClick = {
                    val amountValue = viewModel.getCurrentAmountValue()
                    // Log.d("AmountEntryScreen", "Next Clicked. Amount Value: $amountValue")
                    viewModel.onNextClick()
                    Toast.makeText(context, "Proceeding with $formattedAmount", Toast.LENGTH_SHORT).show()
                    // TODO: Implement navigation to QR Scan Screen
                },
                enabled = isNextEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) // Padding from screen bottom
                    .height(52.dp)
            ) {
                Text("Next", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

// Keypad and KeypadButton composables remain unchanged from previous version...
// --- Keypad Composable (Unchanged) ---
@Composable
fun Keypad(
    onDigitClick: (Char) -> Unit,
    onDecimalClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val keypadLayout = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf('.', '0', 'D')
        )

        keypadLayout.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { key ->
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
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

// --- Individual Keypad Button (Unchanged) ---
@Composable
fun KeypadButton(
    key: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (key == 'D') {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                modifier = Modifier.size(30.dp)
            )
        } else {
            Text(
                text = key.toString(),
                fontSize = 28.sp
            )
        }
    }
}


// --- Preview Function for the entire screen ---
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun AmountEntryScreenPreview() {
    MerchantAppTheme {
        // MODIFIED: Provide dummy lambda for preview
        AmountEntryScreen(onLogoutRequest = {})
    }
}

// --- Preview for the Keypad Component (Unchanged) ---
@Preview(showBackground = true)
@Composable
fun KeypadPreview() {
    MerchantAppTheme {
        Box(modifier = Modifier.height(300.dp)) {
            Keypad(onDigitClick = {}, onDecimalClick = {}, onDeleteClick = {})
        }
    }
}
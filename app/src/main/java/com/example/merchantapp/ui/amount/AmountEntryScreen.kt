package com.example.merchantapp.ui.amount

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.R
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.AmountEntryViewModel
import com.example.merchantapp.viewmodel.AmountEntryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountEntryScreen(
    modifier: Modifier = Modifier,
    viewModel: AmountEntryViewModel = viewModel(),
    onLogoutRequest: () -> Unit,
    onNavigateToQrScan: (String) -> Unit
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
                title = { Text("Food Market") }, // Placeholder Title
                navigationIcon = { Spacer(Modifier.width(48.dp)) },
                actions = {
                    IconButton(onClick = onLogoutRequest) {
                        Icon(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.action_logout))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier // Use modifier from caller (MainScreen)
                .padding(paddingValues) // Apply padding from this Scaffold
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Add consistent horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Transaction Amount", // Consider using stringResource
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
            Spacer(modifier = Modifier.height(16.dp))

            Keypad(
                onDigitClick = viewModel::onDigitPress,
                onDecimalClick = viewModel::onDecimalPress,
                onDeleteClick = viewModel::onDeletePress,
                modifier = Modifier
                    .weight(1f) // Keypad takes available space
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )

            Button(
                onClick = {
                    val rawAmount = uiState.amountString // Use correct field name
                    Log.d("AmountEntryScreen", "Next Clicked. Raw Amount: $rawAmount")
                    onNavigateToQrScan(rawAmount) // Call the navigation callback
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
        verticalArrangement = Arrangement.SpaceEvenly // Distribute rows evenly
    ) {
        val keypadLayout = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf('.', '0', 'D') // 'D' for Delete
        )

        keypadLayout.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Each row takes equal vertical space
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
                            .fillMaxHeight() // Button fills row height
                            .weight(1f)      // Buttons share horizontal space
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    key: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton( // Or Button, OutlinedButton as desired
        onClick = onClick,
        modifier = modifier.padding(4.dp),
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

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun AmountEntryScreenPreview() {
    MerchantAppTheme {
        AmountEntryScreen(onLogoutRequest = {}, onNavigateToQrScan = {})
    }
}
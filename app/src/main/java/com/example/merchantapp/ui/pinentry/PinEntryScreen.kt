// PinEntryScreen.kt
package com.example.merchantapp.ui.pinentry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.border // Keep this import
import androidx.lifecycle.SavedStateHandle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.PinEntryViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(
    viewModel: PinEntryViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onPinVerifiedNavigateToSuccess: (amount: String, beneficiaryId: String, beneficiaryName: String, category: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isPinVerifiedSuccessfully) {
        if (uiState.isPinVerifiedSuccessfully) {
            focusManager.clearFocus()
            onPinVerifiedNavigateToSuccess(
                uiState.amount,
                uiState.beneficiaryId,
                uiState.beneficiaryName,
                uiState.category
            )
            viewModel.onSuccessfulNavigationConsumed()
        }
    }

    val formattedAmount = remember(uiState.amount) {
        try {
            val amountValue = uiState.amount.toDoubleOrNull() ?: 0.0
            // MODIFIED: Changed Locale to th_TH for Thai Baht
            NumberFormat.getCurrencyInstance(Locale("th", "TH")).format(amountValue)
        } catch (e: Exception) {
            "Error" // Fallback for formatting error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter PIN") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Confirm Transaction", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Amount:", formattedAmount)
                    DetailRow("To:", uiState.beneficiaryName)
                    DetailRow("Account ID:", uiState.beneficiaryId)
                    DetailRow("Category:", uiState.category)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Enter your 4-digit PIN to authorize this transaction.", textAlign = TextAlign.Center)

            PinInputFields(
                pinValue = uiState.pinValue,
                onPinChange = { viewModel.onPinChange(it) },
                pinLength = PinEntryViewModel.MAX_PIN_LENGTH,
                enabled = !uiState.isLoading && !uiState.isLocked
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.isLocked) {
                Text(
                    // Consider updating this message if PinEntryViewModel's locked message is more specific
                    "PIN entry locked. Please contact support or try again later.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}

@Composable
fun PinInputFields(
    pinValue: String,
    onPinChange: (String) -> Unit,
    pinLength: Int,
    enabled: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(pinValue, TextRange(pinValue.length)))
    }

    LaunchedEffect(pinValue) {
        if (textFieldValue.text != pinValue) {
            textFieldValue = TextFieldValue(pinValue, TextRange(pinValue.length))
        }
    }

    LaunchedEffect(Unit) {
        if (enabled) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            if (it.text.length <= pinLength && it.text.all { char -> char.isDigit() }) {
                textFieldValue = it
                onPinChange(it.text)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        enabled = enabled,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pinLength) { index ->
                    val char = when {
                        index < textFieldValue.text.length -> textFieldValue.text[index].toString()
                        else -> ""
                    }
                    PinDigitBox(
                        digit = char,
                        isFocused = index == textFieldValue.text.length && enabled
                    )
                    if (index < pinLength - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    )
}

@Composable
fun PinDigitBox(digit: String, isFocused: Boolean) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .border(
                BorderStroke(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.ifEmpty { "" },
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "PIN Entry Screen - Default")
@Composable
fun PinEntryScreenPreview() {
    MerchantAppTheme {
        PinEntryScreen(
            viewModel = PinEntryViewModel(SavedStateHandle(mapOf(
                "amount" to "1230.45", // Example THB amount
                "beneficiaryId" to "BEN-001",
                "beneficiaryName" to "John Doe",
                "category" to "Groceries"
            ))),
            onNavigateBack = {},
            onPinVerifiedNavigateToSuccess = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "PIN Entry Screen - Error")
@Composable
fun PinEntryScreenErrorPreview() {
    val viewModel = PinEntryViewModel(SavedStateHandle(mapOf(
        "amount" to "500.00", // Example THB amount
        "beneficiaryId" to "BEN-002",
        "beneficiaryName" to "Jane Smith",
        "category" to "Utilities"
    )))
    val errorState = PinEntryUiState(
        amount = "500.00",
        beneficiaryId = "BEN-002",
        beneficiaryName = "Jane Smith",
        category = "Utilities",
        pinValue = "111",
        errorMessage = "Incorrect PIN. 6 attempts remaining.", // Updated for 7 attempts
        attemptsRemaining = 6 // Updated for 7 attempts
    )
    MerchantAppTheme {
        PinEntryScreen(
            viewModel = viewModel,
            onNavigateBack = {},
            onPinVerifiedNavigateToSuccess = { _, _, _, _ -> }
        )
    }
}
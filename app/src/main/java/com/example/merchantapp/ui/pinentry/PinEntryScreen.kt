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
import androidx.compose.foundation.border
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
            focusManager.clearFocus() // Clear focus before navigating
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
            NumberFormat.getCurrencyInstance(Locale("en", "US")).format(amountValue)
        } catch (e: Exception) {
            "Error"
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

            // Display transaction details (optional, but good for confirmation)
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
                    "PIN entry locked. Please contact support or try again later.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // "Verify PIN" button is removed as auto-submission is implemented
            // If you prefer manual submission, you can add a Button here:
            /*
            Button(
                onClick = { viewModel.verifyPin() },
                enabled = !uiState.isLoading && !uiState.isLocked && uiState.pinValue.length == PinEntryViewModel.MAX_PIN_LENGTH,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Verify PIN")
            }
            */
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

    // Update TextFieldValue when pinValue from ViewModel changes (e.g., on error reset)
    LaunchedEffect(pinValue) {
        if (textFieldValue.text != pinValue) {
            textFieldValue = TextFieldValue(pinValue, TextRange(pinValue.length))
        }
    }

    // Request focus when the composable enters the composition and is enabled
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
                        isFocused = index == textFieldValue.text.length && enabled // Highlight next empty box if focused
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
            text = digit.ifEmpty { "" }, // Show digit or empty
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp, // Adjust font size as needed
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
                "amount" to "123.45",
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
        "amount" to "50.00",
        "beneficiaryId" to "BEN-002",
        "beneficiaryName" to "Jane Smith",
        "category" to "Utilities"
    )))
    // Simulate an error state directly in ViewModel for preview (not ideal for real app but ok for preview)
    // This requires making parts of uiState or methods public if they aren't, or a more complex preview setup.
    // For simplicity, we'll rely on the ViewModel's internal logic that could lead to an error state.
    // A better approach is to have a content composable taking PinEntryUiState directly.

    // Simulate a state with an error message
    val errorState = PinEntryUiState(
        amount = "50.00",
        beneficiaryId = "BEN-002",
        beneficiaryName = "Jane Smith",
        category = "Utilities",
        pinValue = "111", // Incomplete PIN
        errorMessage = "Incorrect PIN. 2 attempts remaining.",
        attemptsRemaining = 2
    )
    // We can't easily inject a pre-configured state directly if the screen uses `viewModel()`
    // For now, this preview will show the initial state of the VM.

    MerchantAppTheme {
        // To show the error state in preview more directly, you'd refactor PinEntryScreen
        // to take PinEntryUiState as a parameter, similar to SetNewPasswordScreenContent.
        // For now, this preview will just show the VM's initial state.
        PinEntryScreen(
            viewModel = viewModel, // This VM will be in its initial state
            onNavigateBack = {},
            onPinVerifiedNavigateToSuccess = { _, _, _, _ -> }
        )
    }
}
package com.example.merchantapp.ui.pinentry

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.PinEntryViewModel // Ensure PinEntryViewModel is correctly imported
import java.text.NumberFormat
import java.util.Locale

// ViewModel Factory for AndroidViewModel if not using Hilt
class PinEntryViewModelFactory(private val application: Application, private val savedStateHandle: SavedStateHandle) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PinEntryViewModel(application, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for PinEntryViewModelFactory")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(
    // Use this if you need to pass SavedStateHandle to the factory for arguments from navigation
    // viewModel: PinEntryViewModel = viewModel(factory = PinEntryViewModelFactory(LocalContext.current.applicationContext as Application, LocalSavedStateRegistryOwner.current.savedStateRegistry.consumeRestoredStateForKey("pinEntryArgs")?.let { SavedStateHandle.createHandle(it, null) } ?: SavedStateHandle())),
    // Simpler instantiation if SavedStateHandle is automatically provided by compose-navigation to viewModels
    viewModel: PinEntryViewModel = viewModel(), // Standard way if SavedStateHandle is implicitly passed by navigation-compose
    onNavigateBack: () -> Unit,
    // <<< MODIFIED LAMBDA SIGNATURE HERE >>>
    onPinVerifiedNavigateToSuccess: (
        amount: String,
        beneficiaryId: String,
        beneficiaryName: String,
        category: String,
        transactionId: String // Added transactionId
    ) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Effect to handle navigation when PIN verification is successful
    LaunchedEffect(uiState.isPinVerifiedSuccessfully, uiState.transactionIdForSuccess) { // Observe transactionIdForSuccess too
        if (uiState.isPinVerifiedSuccessfully && uiState.transactionIdForSuccess != null) {
            focusManager.clearFocus()
            onPinVerifiedNavigateToSuccess(
                uiState.amount,
                uiState.beneficiaryId,
                uiState.beneficiaryName,
                uiState.category,
                uiState.transactionIdForSuccess!! // Pass the transactionId (non-null due to check)
            )
            viewModel.onSuccessfulNavigationConsumed()
        }
    }

    val formattedAmount = remember(uiState.amount) {
        try {
            val amountValue = uiState.amount.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale("th", "TH")).format(amountValue)
        } catch (e: Exception) {
            "Error"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Beneficiary PIN") },
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

            Text("Confirm Transaction Details", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Amount:", formattedAmount)
                    DetailRow("To (Beneficiary):", uiState.beneficiaryName)
                    DetailRow("Beneficiary Account ID:", uiState.beneficiaryId)
                    DetailRow("Category:", uiState.category)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Enter the beneficiary's 4-digit PIN to authorize this payment from their account.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

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
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.6f))
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
    var textFieldValue by remember(pinValue) {
        mutableStateOf(TextFieldValue(pinValue, TextRange(pinValue.length)))
    }

    LaunchedEffect(enabled) {
        if (enabled && pinValue.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            if (newTextFieldValue.text.length <= pinLength && newTextFieldValue.text.all { char -> char.isDigit() }) {
                textFieldValue = newTextFieldValue
                onPinChange(newTextFieldValue.text)
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
                    val char = textFieldValue.text.getOrNull(index)
                    PinDigitBox(
                        digit = char?.toString() ?: "",
                        isFocused = index == textFieldValue.text.length && enabled && textFieldValue.text.length < pinLength
                    )
                    if (index < pinLength - 1) {
                        Spacer(modifier = Modifier.width(12.dp))
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
            .size(width = 50.dp, height = 60.dp)
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
            text = if (digit.isNotEmpty()) "●" else "",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        )
    }
}


// --- PREVIEWS ---
// Helper function for creating a SavedStateHandle for Previews if needed
fun createPreviewSavedStateHandle(initialState: Map<String, Any?> = emptyMap()): SavedStateHandle {
    return SavedStateHandle(initialState)
}

@Preview(showBackground = true, name = "PIN Entry Screen - Default")
@Composable
private fun PinEntryScreenPreview() {
    MerchantAppTheme {
        val dummyApplication = LocalContext.current.applicationContext as Application
        val dummySavedStateHandle = createPreviewSavedStateHandle(mapOf(
            "amount" to "123.45",
            "beneficiaryId" to "BEN-ID-001",
            "beneficiaryName" to "Test Beneficiary",
            "category" to "Groceries"
        ))
        // Use the factory for preview consistency if ViewModel takes SavedStateHandle
        PinEntryScreen(
            viewModel = PinEntryViewModel(dummyApplication, dummySavedStateHandle),
            onNavigateBack = {},
            // Preview lambda now matches the 5 parameters
            onPinVerifiedNavigateToSuccess = { _, _, _, _, _ -> }
        )
    }
}
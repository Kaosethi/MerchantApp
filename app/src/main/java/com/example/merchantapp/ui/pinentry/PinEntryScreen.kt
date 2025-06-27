package com.example.merchantapp.ui.pinentry

import android.app.Application
import android.os.Bundle // <<< NEW IMPORT for AbstractSavedStateViewModelFactory
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AbstractSavedStateViewModelFactory // <<< NEW IMPORT
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
// ViewModelProvider import is no longer explicitly needed here if using AbstractSavedStateViewModelFactory correctly with viewModel()
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistryOwner // <<< NEW IMPORT for AbstractSavedStateViewModelFactory
import com.example.merchantapp.ui.outcome.OutcomeType
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.PinEntryViewModel
import com.example.merchantapp.viewmodel.OutcomeNavArgs
import java.text.NumberFormat
import java.util.Locale

// --- MODIFIED ViewModel Factory ---
class PinEntryViewModelFactory(
    owner: SavedStateRegistryOwner, // Required by AbstractSavedStateViewModelFactory
    private val application: Application,
    defaultArgs: Bundle? = null // Optional default arguments Bundle
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String, // Name of the ViewModel class (unused here but required by override)
        modelClass: Class<T>,
        handle: SavedStateHandle // This 'handle' is provided by the factory, already populated
    ): T {
        if (modelClass.isAssignableFrom(PinEntryViewModel::class.java)) {
            // The 'handle' parameter here is the one correctly populated with nav args
            return PinEntryViewModel(application, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
// --- END MODIFIED ViewModel Factory ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(
    // ViewModel will now be instantiated in MainActivity's NavHost using the updated factory
    viewModel: PinEntryViewModel, // Pass the already created ViewModel
    onNavigateBack: () -> Unit,
    onPinVerifiedNavigateToSuccess: (
        transactionId: String,
        paymentId: String,
        amount: String,
        beneficiaryName: String,
        category: String
    ) -> Unit,
    onNavigateToOutcome: (
        outcomeType: OutcomeType,
        title: String,
        message: String,
        buttonLabel: String
    ) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.isPinVerifiedSuccessfully, uiState.transactionIdForSuccess) {
        if (uiState.isPinVerifiedSuccessfully && uiState.transactionIdForSuccess != null) {
            keyboardController?.hide()
            focusManager.clearFocus()
            onPinVerifiedNavigateToSuccess(
                uiState.transactionIdForSuccess!!,  // trxId
                uiState.beneficiaryId,              // payId
                uiState.amount,                     // amt
                uiState.beneficiaryName,            // name
                uiState.category                    // category
            )
            viewModel.onSuccessfulNavigationConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToOutcomeScreenEvent.collect { navArgs ->
            keyboardController?.hide()
            focusManager.clearFocus()
            onNavigateToOutcome(
                navArgs.outcomeType,
                navArgs.title,
                navArgs.message,
                navArgs.buttonLabel
            )
            // viewModel.onOutcomeNavigationObserved() // Optional
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
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Confirm Transaction Details", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Amount:", formattedAmount)
                    DetailRow("To (Beneficiary):", uiState.beneficiaryName)
                    DetailRow("Category:", uiState.category)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Enter the PIN to authorize this payment.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            PinInputFields(
                pinValue = uiState.pinValue,
                onPinChange = { viewModel.onPinChange(it) },
                pinLength = PinEntryViewModel.MAX_PIN_LENGTH,
                enabled = !uiState.isLoading && !uiState.isLocked
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                if (uiState.isLocked && uiState.errorMessage == null) {
                    Text(
                        "PIN entry is locked.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                } else if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(0.4f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
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
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(enabled) {
        if (enabled && pinValue.isEmpty()) {
            focusRequester.requestFocus()
            keyboardController?.show()
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
        modifier = Modifier.focusRequester(focusRequester),
        enabled = enabled,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pinLength) { index ->
                    val char = textFieldValue.text.getOrNull(index)
                    val hasFocus = enabled && index == textFieldValue.text.length && textFieldValue.text.length < pinLength
                    PinDigitBox(
                        digit = char?.toString() ?: "",
                        isFocused = hasFocus
                    )
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
                    width = if (isFocused) 2.dp else 1.5.dp,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (digit.isNotEmpty()) "●" else "",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

// Helper for Previews - creating SavedStateHandle (no change here, but its usage in preview might change)
fun createPreviewSavedStateHandle(initialState: Map<String, Any?> = emptyMap()): SavedStateHandle {
    return SavedStateHandle(initialState)
}


// --- PREVIEWS MIGHT NEED ADJUSTMENT ---
// Since PinEntryScreen now expects a ViewModel instance directly,
// previews need to provide a real or fake ViewModel instance.
// The factory won't be directly called within PinEntryScreen's composable anymore for default instantiation.
@Preview(showBackground = true, name = "PIN Entry Screen - Default")
@Composable
private fun PinEntryScreenPreviewDefault() {
    MerchantAppTheme {
        val context = LocalContext.current
        // For previews, you often create a dummy ViewModel instance or use a fake.
        // The factory helps if you instantiate it at the NavHost level.
        // Here, we'll simulate providing a VM directly.
        val previewApp = context.applicationContext as Application
        val previewSsh = createPreviewSavedStateHandle(mapOf("amount" to "100.00", "beneficiaryName" to "Coffee Shop", "category" to "Food"))
        val previewViewModel = PinEntryViewModel(previewApp, previewSsh)

        PinEntryScreen(
            viewModel = previewViewModel, // Pass the created VM
            onNavigateBack = {},
            onPinVerifiedNavigateToSuccess = { _, _, _, _, _ -> },
            onNavigateToOutcome = {_,_,_,_ ->}
        )
    }
}

// Similar adjustments for other previews if they rely on viewModel() default instantiation.
// The key is that PinEntryScreen now receives 'viewModel: PinEntryViewModel' as a parameter.
// How this viewModel is created (either by viewModel() in NavHost or manually for preview) is external to PinEntryScreen itself.

@Preview(showBackground = true, name = "PIN Entry Screen - Error")
@Composable
private fun PinEntryScreenPreviewError() {
    MerchantAppTheme {
        val context = LocalContext.current
        val previewApp = context.applicationContext as Application
        val previewSsh = createPreviewSavedStateHandle(mapOf("amount" to "50.50", "beneficiaryName" to "Book Store", "category" to "Shopping"))
        val previewViewModel = PinEntryViewModel(previewApp, previewSsh)
        // To show error state in preview, you'd ideally have the ViewModel emit this state
        // or use a fake ViewModel. For a quick preview, you might try to update its state
        // if the _uiState were public, but that's not good practice for real ViewModels.
        // Best: Create a FakePinEntryViewModel for previews that can be put into desired states.
        // For now, this preview will show the initial state.
        // To see error: Run the app and trigger an error.

        PinEntryScreen(
            viewModel = previewViewModel,
            onNavigateBack = {},
            onPinVerifiedNavigateToSuccess = { _, _, _, _, _ -> },
            onNavigateToOutcome = {_,_,_,_ ->}
        )
    }
}

@Preview(showBackground = true, name = "PIN Entry Screen - Locked")
@Composable
private fun PinEntryScreenPreviewLocked() {
    MerchantAppTheme {
        val context = LocalContext.current
        val previewApp = context.applicationContext as Application
        // --- MODIFIED MAP CREATION ---
        val previewSsh = createPreviewSavedStateHandle(mapOf(
            Pair("amount", "20.00"),
            Pair("beneficiaryName", "Cinema"),
            Pair("category", "Entertainment")
        ))
        val previewViewModel = PinEntryViewModel(previewApp, previewSsh)
        // Similar to error preview for showing locked state.

        PinEntryScreen(
            viewModel = previewViewModel,
            onNavigateBack = {},
            onPinVerifiedNavigateToSuccess = { _, _, _, _, _ -> },
            onNavigateToOutcome = {_,_,_,_ ->}
        )
    }
}
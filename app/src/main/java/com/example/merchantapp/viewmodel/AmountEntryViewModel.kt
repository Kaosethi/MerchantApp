// File path: app/src/main/java/com/example/merchantapp/viewmodel/AmountEntryViewModel.kt
package com.example.merchantapp.viewmodel

// --- Required Imports ---
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Locale
// --- End Required Imports ---

data class AmountEntryUiState(
    val amountString: String = "",
    val inputError: String? = null
)

// Ignore "never used" warning for the class - it's used by viewModel() in the Screen
class AmountEntryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AmountEntryUiState())
    // Ignore "never used" warning - used internally and implicitly by collectors
    val uiState: StateFlow<AmountEntryUiState> = _uiState.asStateFlow()

    private val maxDigitsBeforeDecimal = 7
    private val maxDigitsAfterDecimal = 2
    private val maxTotalLength = maxDigitsBeforeDecimal + maxDigitsAfterDecimal + 1

    // Ignore "never used" warnings - called from Screen
    fun onDigitPress(digit: Char) {
        _uiState.update { currentState ->
            val currentInput = currentState.amountString
            var nextInput = currentInput

            if (currentInput == "0" && digit != '0') {
                nextInput = digit.toString()
            } else if (currentInput.isEmpty() && digit == '0') {
                nextInput = "0"
            } else if (currentInput != "0") {
                nextInput += digit
            }
            validateInput(nextInput, currentState)
        }
    }

    // Ignore "never used" warning - called from Screen
    fun onDecimalPress() {
        _uiState.update { currentState ->
            val currentInput = currentState.amountString
            var nextInput = currentInput
            if (!currentInput.contains('.')) {
                if (currentInput.isEmpty()) {
                    nextInput = "0."
                } else {
                    nextInput += "."
                }
            }
            validateInput(nextInput, currentState)
        }
    }

    // Ignore "never used" warning - called from Screen
    fun onDeletePress() {
        _uiState.update { currentState ->
            if (currentState.amountString.isNotEmpty()) {
                val nextInput = currentState.amountString.dropLast(1)
                currentState.copy(amountString = nextInput, inputError = null)
            } else {
                currentState
            }
        }
    }

    private fun validateInput(input: String, previousState: AmountEntryUiState): AmountEntryUiState {
        // Ignore "error is always null" warning - it's assigned conditionally below
        var error: String? = null
        // CHANGED: Use val as suggested, since it's not reassigned within this specific function scope
        val validatedInput = input // (Line 79)

        if (validatedInput.length > maxTotalLength) {
            error = "Maximum amount length reached"
            // Return previous state but with the error message
            return previousState.copy(inputError = error)
        }
        val parts = validatedInput.split('.')
        if (parts[0].length > maxDigitsBeforeDecimal) {
            error = "Amount too large"
            return previousState.copy(inputError = error)
        }
        if (parts.size > 1 && parts[1].length > maxDigitsAfterDecimal) {
            error = "Maximum $maxDigitsAfterDecimal decimal places allowed"
            return previousState.copy(inputError = error)
        }
        // If no errors found, return the new input and clear any previous error
        return AmountEntryUiState(amountString = validatedInput, inputError = null) // Use new state constructor
    }

    // Ignore "never used" warning - observed in Screen
    val formattedAmount: StateFlow<String> = _uiState
        .map { formatCurrency(it.amountString) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = formatCurrency("")
        )

    // Ignore "never used" warning - observed in Screen
    val isNextEnabled: StateFlow<Boolean> = _uiState
        .map { (it.amountString.toDoubleOrNull() ?: 0.0) > 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private fun formatCurrency(amountStr: String): String {
        return try {
            if (amountStr.isBlank()) return "$0.00"
            val valueToFormat = if (amountStr.endsWith('.')) amountStr + "0"
            else if (amountStr == ".") "0.0"
            else amountStr
            val amountValue = valueToFormat.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale.US).format(amountValue)
        } catch (e: NumberFormatException) {
            Log.e("AmountEntryVM", "Error formatting currency: $amountStr", e)
            "$0.00"
        }
    }

    // Ignore "never used" warning - called from Screen
    fun onNextClick() {
        val currentAmount = getCurrentAmountValue() // Calls the function below
        if (currentAmount > 0) {
            Log.i("AmountEntryVM", "Next clicked. Valid amount: $currentAmount")
            // TODO: Trigger navigation
        } else {
            Log.w("AmountEntryVM", "Next clicked. Invalid amount: $currentAmount")
            _uiState.update { it.copy(inputError = "Amount must be greater than zero") }
        }
    }

    // Ignore "could be private" warning - called from Screen
    fun getCurrentAmountValue(): Double {
        return _uiState.value.amountString.toDoubleOrNull() ?: 0.0
    }

    // Ignore "never used" warning - potentially called from Screen (if uncommented there)
    fun clearInputError() {
        _uiState.update { it.copy(inputError = null) }
    }
}
package com.example.merchantapp.viewmodel

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

data class AmountEntryUiState(
    val amountString: String = "", // Holds the raw input string
    val inputError: String? = null
)

class AmountEntryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AmountEntryUiState())
    val uiState: StateFlow<AmountEntryUiState> = _uiState.asStateFlow()

    private val maxDigitsBeforeDecimal = 7
    private val maxDigitsAfterDecimal = 2
    private val maxTotalLength = maxDigitsBeforeDecimal + maxDigitsAfterDecimal + 1

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

    fun onDeletePress() {
        _uiState.update { currentState ->
            if (currentState.amountString.isNotEmpty()) {
                val nextInput = currentState.amountString.dropLast(1)
                AmountEntryUiState(amountString = nextInput, inputError = null)
            } else {
                currentState
            }
        }
    }

    private fun validateInput(input: String, previousState: AmountEntryUiState): AmountEntryUiState {
        var error: String? = null
        val validatedInput = input

        if (validatedInput.length > maxTotalLength) {
            error = "Maximum amount length reached"
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
        return AmountEntryUiState(amountString = validatedInput, inputError = null)
    }

    val formattedAmount: StateFlow<String> = _uiState
        .map { formatCurrency(it.amountString) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = formatCurrency("")
        )

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

    fun clearInputError() {
        _uiState.update { it.copy(inputError = null) }
    }
}
// File: app/src/main/java/com/example/merchantapp/viewmodel/AmountEntryViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.DecimalFormat

/**
 * UI state for the Amount Entry screen.
 */
data class AmountEntryUiState(
    val amount: String = "", // Raw input string, e.g., "123.45"
    val displayAmount: String = "$0.00", // Formatted string for display
    val isAmountValid: Boolean = false // Derived state: is the amount valid (> 0)?
)

/**
 * ViewModel for the Amount Entry screen using Numpad.
 */
class AmountEntryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AmountEntryUiState())
    val uiState: StateFlow<AmountEntryUiState> = _uiState.asStateFlow()

    // Define max length if needed, e.g., before decimal point
    private val maxIntegerPartLength = 7 // Example: allow up to 9,999,999

    // Function called when a digit (0-9) is pressed
    fun onDigitClick(digit: Char) {
        val currentAmount = _uiState.value.amount
        val decimalPointIndex = currentAmount.indexOf('.')
        var newAmount = currentAmount

        if (decimalPointIndex != -1) {
            // Already has a decimal point
            val decimalPartLength = currentAmount.length - decimalPointIndex - 1
            if (decimalPartLength < 2) { // Only allow 2 decimal places
                newAmount += digit
            }
        } else {
            // No decimal point yet
            if (currentAmount.length < maxIntegerPartLength) {
                // Handle leading zero: if current is "0", replace it unless next is "."
                if (currentAmount == "0") {
                    newAmount = digit.toString()
                } else {
                    newAmount += digit
                }
            }
        }
        updateState(newAmount)
    }

    // Function called when the decimal point is pressed
    fun onDecimalClick() {
        val currentAmount = _uiState.value.amount
        // Add decimal only if it doesn't exist yet
        if (!currentAmount.contains('.')) {
            // If empty, prefix with "0"
            val newAmount = if (currentAmount.isEmpty()) "0." else "$currentAmount."
            updateState(newAmount)
        }
    }

    // Function called when backspace is pressed
    fun onBackspaceClick() {
        val currentAmount = _uiState.value.amount
        if (currentAmount.isNotEmpty()) {
            val newAmount = currentAmount.dropLast(1)
            // If backspace leaves just "0", reset to empty for consistency? Or keep "0"? Let's keep "0".
            // If backspace removes the ".", update normally.
            updateState(newAmount)
        }
    }

    // Central function to update state and recalculate derived values
    private fun updateState(newRawAmount: String) {
        val amountDouble = newRawAmount.toDoubleOrNull()
        val isValid = amountDouble != null && amountDouble > 0.0
        val display = formatForDisplay(newRawAmount)
        Log.d("AmountEntryViewModel", "Updating state. Raw: $newRawAmount, Display: $display, IsValid: $isValid")
        _uiState.update {
            it.copy(
                amount = newRawAmount,
                displayAmount = display,
                isAmountValid = isValid
            )
        }
    }

    // Formatter for display purposes
    private val displayFormatter = DecimalFormat("$#,##0.00")

    // Format the raw amount string for display
    private fun formatForDisplay(rawAmount: String): String {
        // Handle empty or just "." case for display
        if (rawAmount.isEmpty()) return "$0.00"
        if (rawAmount == ".") return "$0." // Show intermediate state

        val amountDouble = rawAmount.toDoubleOrNull() ?: 0.0 // Default to 0 if parsing fails during intermediate input
        if (rawAmount.endsWith('.')) {
            // If user just typed ".", show the integer part formatted + "."
            val integerPart = rawAmount.substringBefore('.').toLongOrNull() ?: 0L
            return DecimalFormat("$#,##0").format(integerPart) + "."

        } else if (rawAmount.contains('.') && rawAmount.substringAfter('.').length == 1){
            // If user has typed one decimal place, show it formatted
            val integerPart = rawAmount.substringBefore('.').toLongOrNull() ?: 0L
            val decimalPart = rawAmount.substringAfter('.')
            return DecimalFormat("$#,##0").format(integerPart) + "." + decimalPart
        } else {
            // Otherwise, format fully
            return displayFormatter.format(amountDouble)
        }

    }

    fun clearAmount() {
        Log.d("AmountEntryViewModel", "Clearing amount")
        _uiState.update { AmountEntryUiState() } // Reset to default state
    }

    // We still need the raw amount for navigation/processing
    fun getRawAmountValue(): String {
        return _uiState.value.amount
    }
}
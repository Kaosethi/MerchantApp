// File: app/src/main/java/com/example/merchantapp/viewmodel/AmountEntryViewModel.kt
package com.example.merchantapp.viewmodel // Or your actual viewmodel package

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.DecimalFormat

data class AmountEntryUiState(
    val amount: String = "", // Raw input string, e.g., "123.45"
    val displayAmount: String = "$0.00", // Formatted string for display
    val isAmountValid: Boolean = false
)

class AmountEntryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AmountEntryUiState())
    val uiState: StateFlow<AmountEntryUiState> = _uiState.asStateFlow()

    private val maxIntegerPartLength = 7

    fun onDigitClick(digit: Char) {
        val currentAmount = _uiState.value.amount
        val decimalPointIndex = currentAmount.indexOf('.')
        var newAmount = currentAmount

        if (decimalPointIndex != -1) {
            val decimalPartLength = currentAmount.length - decimalPointIndex - 1
            if (decimalPartLength < 2) { newAmount += digit }
        } else {
            if (currentAmount.length < maxIntegerPartLength) {
                if (currentAmount == "0") { newAmount = digit.toString() } else { newAmount += digit }
            }
        }
        updateState(newAmount)
    }

    fun onDecimalClick() {
        val currentAmount = _uiState.value.amount
        if (!currentAmount.contains('.')) {
            val newAmount = if (currentAmount.isEmpty()) "0." else "$currentAmount."
            updateState(newAmount)
        }
    }

    fun onBackspaceClick() {
        val currentAmount = _uiState.value.amount
        if (currentAmount.isNotEmpty()) {
            val newAmount = currentAmount.dropLast(1)
            updateState(newAmount)
        }
    }

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

    private val displayFormatter = DecimalFormat("$#,##0.00")
    private val integerFormatter = DecimalFormat("$#,##0") // Added for partial formatting

    private fun formatForDisplay(rawAmount: String): String {
        if (rawAmount.isEmpty()) return "$0.00"
        if (rawAmount == ".") return "$0."

        val amountDouble = rawAmount.toDoubleOrNull() ?: 0.0
        if (rawAmount.endsWith('.')) {
            val integerPart = rawAmount.substringBefore('.').toLongOrNull() ?: 0L
            return integerFormatter.format(integerPart) + "."
        } else if (rawAmount.contains('.') && rawAmount.substringAfter('.').length == 1){
            val integerPart = rawAmount.substringBefore('.').toLongOrNull() ?: 0L
            val decimalPart = rawAmount.substringAfter('.')
            return integerFormatter.format(integerPart) + "." + decimalPart
        } else {
            return displayFormatter.format(amountDouble)
        }
    }

    fun clearAmount() {
        Log.d("AmountEntryViewModel", "Clearing amount")
        _uiState.update { AmountEntryUiState() }
    }

    fun getRawAmountValue(): String {
        return _uiState.value.amount
    }
}
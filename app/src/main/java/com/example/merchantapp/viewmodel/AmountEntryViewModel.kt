// File: app/src/main/java/com/example/merchantapp/viewmodel/AmountEntryViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols // ADDED: Import for custom symbols
import java.util.Locale // ADDED: Import Locale

data class AmountEntryUiState(
    val amount: String = "",
    val displayAmount: String = "฿0.00", // MODIFIED: Default display for THB
    val isAmountValid: Boolean = false
)

class AmountEntryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AmountEntryUiState())
    val uiState: StateFlow<AmountEntryUiState> = _uiState.asStateFlow()

    private val maxIntegerPartLength = 7 // Adjust if needed for THB (e.g., 9 for millions)

    // MODIFIED: Setup formatters specifically for THB (฿ symbol)
    private val thaiLocale = Locale("th", "TH")
    private val thaiSymbols = DecimalFormatSymbols(thaiLocale).apply {
        currencySymbol = "฿"
    }
    private val displayFormatter = DecimalFormat("#,##0.00", thaiSymbols).apply {
        // Ensure currency symbol is used (though it's in thaiSymbols now)
        // This format pattern assumes standard grouping/decimal for THB locale,
        // overridden currency symbol.
    }
    private val integerFormatter = DecimalFormat("#,##0", thaiSymbols).apply {
        // Formatter for when typing integer part or just the decimal point
    }

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

    private fun formatForDisplay(rawAmount: String): String {
        // Use the THB specific formatters
        if (rawAmount.isEmpty()) return "฿0.00" // MODIFIED: Use ฿
        if (rawAmount == ".") return "฿0." // MODIFIED: Use ฿

        // When typing decimal part, manually construct string with correct symbol
        if (rawAmount.endsWith('.')) {
            val integerPart = rawAmount.substringBefore('.').toLongOrNull() ?: 0L
            // Format integer part, add symbol and decimal point
            return thaiSymbols.currencySymbol + integerFormatter.format(integerPart) + "."
        } else if (rawAmount.contains('.') && rawAmount.substringAfter('.').length == 1){
            val integerPart = rawAmount.substringBefore('.').toLongOrNull() ?: 0L
            val decimalPart = rawAmount.substringAfter('.')
            // Format integer part, add symbol, decimal point and the single decimal digit
            return thaiSymbols.currencySymbol + integerFormatter.format(integerPart) + "." + decimalPart
        } else {
            // For complete numbers (integer or with 2 decimals), use the full display formatter
            val amountDouble = rawAmount.toDoubleOrNull() ?: 0.0
            // Prepend symbol explicitly as the pattern might not guarantee it depending on exact DecimalFormat behavior
            return thaiSymbols.currencySymbol + displayFormatter.format(amountDouble)
        }
    }

    fun clearAmount() {
        Log.d("AmountEntryViewModel", "Clearing amount")
        _uiState.update { AmountEntryUiState() } // Resets to default state with ฿0.00
    }

    fun getRawAmountValue(): String {
        // Ensure the raw value doesn't somehow contain the currency symbol before sending
        return _uiState.value.amount.trim()
    }
}
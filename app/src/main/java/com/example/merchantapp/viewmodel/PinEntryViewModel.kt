// PinEntryViewModel.kt
package com.example.merchantapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.ui.pinentry.PinEntryUiState // Ensure this import path is correct
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ADDED: Entire new file
class PinEntryViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinEntryUiState())
    val uiState: StateFlow<PinEntryUiState> = _uiState.asStateFlow()

    // Hardcoded correct PIN for mocking purposes
    private val correctPin = "1234" // This should match the PIN set during onboarding (mocked)

    companion object {
        const val MAX_PIN_LENGTH = 4
        const val MAX_ATTEMPTS = 8
    }

    init {
        val amount = savedStateHandle.get<String>("amount") ?: "0.00"
        val beneficiaryId = savedStateHandle.get<String>("beneficiaryId") ?: "N/A"
        val beneficiaryName = savedStateHandle.get<String>("beneficiaryName") ?: "Unknown"
        val category = savedStateHandle.get<String>("category") ?: "Default"
        // Note: If category was URL-encoded, decode it here.

        _uiState.update {
            it.copy(
                amount = amount,
                beneficiaryId = beneficiaryId,
                beneficiaryName = beneficiaryName,
                category = category,
                attemptsRemaining = MAX_ATTEMPTS // Initialize attempts
            )
        }
    }

    fun onPinChange(newPinValue: String) {
        if (newPinValue.length <= MAX_PIN_LENGTH && newPinValue.all { it.isDigit() }) {
            _uiState.update {
                it.copy(
                    pinValue = newPinValue,
                    errorMessage = null // Clear error on input change
                )
            }
            // Auto-submit if PIN reaches max length
            if (newPinValue.length == MAX_PIN_LENGTH) {
                verifyPin()
            }
        }
    }

    fun verifyPin() {
        if (_uiState.value.isLocked) {
            _uiState.update { it.copy(errorMessage = "PIN entry is locked due to too many attempts.") }
            return
        }

        if (_uiState.value.pinValue.length != MAX_PIN_LENGTH) {
            _uiState.update { it.copy(errorMessage = "PIN must be $MAX_PIN_LENGTH digits.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1000) // Simulate network latency for PIN verification

            if (_uiState.value.pinValue == correctPin) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPinVerifiedSuccessfully = true,
                        errorMessage = null
                        // Optionally reset attemptsRemaining here or handle it differently
                    )
                }
            } else {
                val newAttemptsRemaining = _uiState.value.attemptsRemaining - 1
                val isNowLocked = newAttemptsRemaining <= 0
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Incorrect PIN. ${if (!isNowLocked) "$newAttemptsRemaining attempts remaining." else "PIN entry locked."}",
                        pinValue = "", // Clear PIN field on error
                        attemptsRemaining = newAttemptsRemaining,
                        isLocked = isNowLocked
                    )
                }
            }
        }
    }

    fun onSuccessfulNavigationConsumed() {
        _uiState.update { it.copy(isPinVerifiedSuccessfully = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
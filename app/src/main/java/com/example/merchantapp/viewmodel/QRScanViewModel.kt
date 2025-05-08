// QrScanViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ADDED: Entire new file

data class QrScanUiState(
    val isLoading: Boolean = false,
    val amount: String = "0.00", // Amount passed from previous screen
    val scannedQrCodeValue: String? = null,
    val isQrCodeDetected: Boolean = false, // Flag to trigger navigation once detected
    val errorMessage: String? = null
)

class QrScanViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrScanUiState())
    val uiState: StateFlow<QrScanUiState> = _uiState.asStateFlow()

    init {
        val amountArg = savedStateHandle.get<String>("amount") ?: "0.00"
        _uiState.update { it.copy(amount = amountArg) }
        Log.d("QrScanViewModel", "Initialized with amount: $amountArg")
    }

    // Called by the QrCodeAnalyzer when a QR code is successfully detected
    fun onQrCodeScanned(rawValue: String?) {
        if (rawValue.isNullOrBlank()) {
            Log.w("QrScanViewModel", "QR Scan returned null or blank value.")
            // Optionally set an error message or just ignore
            return
        }

        // Prevent processing multiple times if the QR code stays in view
        if (!_uiState.value.isQrCodeDetected) {
            Log.i("QrScanViewModel", "QR Code Detected: $rawValue")
            _uiState.update {
                it.copy(
                    scannedQrCodeValue = rawValue,
                    isQrCodeDetected = true, // Set flag to trigger navigation
                    errorMessage = null
                )
            }
        } else {
            Log.d("QrScanViewModel", "Ignoring subsequent QR code detection: $rawValue")
        }
    }

    // Called by the UI after navigation has been triggered
    fun onNavigationHandled() {
        _uiState.update { it.copy(isQrCodeDetected = false, scannedQrCodeValue = null) }
        Log.d("QrScanViewModel", "Navigation handled, resetting detection flag.")
    }

    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
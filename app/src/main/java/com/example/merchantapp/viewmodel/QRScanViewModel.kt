// QrScanViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.data.QrPayload // ADDED: Import QrPayload
import com.google.gson.Gson // ADDED: Import Gson
import com.google.gson.JsonSyntaxException // ADDED: Import for parsing errors
import kotlinx.coroutines.delay // ADDED: For mocking delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ADDED: Data class for successful validation result
data class ValidatedBeneficiary(
    val id: String,
    val name: String
)

data class QrScanUiState(
    val isLoading: Boolean = false,
    val amount: String = "0.00",
    // REMOVED: scannedQrCodeValue - No longer needed directly in UI state for nav trigger
    // REMOVED: isQrCodeDetected - Replaced by validationSuccess/validatedBeneficiary
    val errorMessage: String? = null,
    // ADDED: State for validation result
    val validationSuccess: Boolean = false,
    val validatedBeneficiary: ValidatedBeneficiary? = null
)

class QrScanViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrScanUiState())
    val uiState: StateFlow<QrScanUiState> = _uiState.asStateFlow()
    private val gson = Gson() // Create Gson instance

    init {
        val amountArg = savedStateHandle.get<String>("amount") ?: "0.00"
        _uiState.update { it.copy(amount = amountArg) }
        Log.d("QrScanViewModel", "Initialized with amount: $amountArg")
    }

    // Called by the QrCodeAnalyzer when a QR code is detected
    fun onQrCodeScanned(rawValue: String?) {
        if (rawValue.isNullOrBlank()) {
            Log.w("QrScanViewModel", "QR Scan returned null or blank value.")
            _uiState.update { it.copy(errorMessage = "Invalid QR Code scanned.", isLoading = false) }
            return
        }
        // Don't immediately navigate, start validation instead
        validateAndFetchBeneficiary(rawValue)
    }

    // --- MODIFIED: Validation and Mock Fetch Logic ---
    private fun validateAndFetchBeneficiary(qrJsonString: String) {
        // Prevent multiple validation calls if already processing or successful
        if (_uiState.value.isLoading || _uiState.value.validationSuccess) {
            Log.d("QrScanViewModel", "Validation already in progress or successful, ignoring new scan.")
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(1000) // Simulate network delay for validation API call

            try {
                // 1. Parse JSON
                val payload = gson.fromJson(qrJsonString, QrPayload::class.java)

                // 2. Basic Format Validation
                if (payload == null || payload.type.isNullOrBlank() || payload.account.isNullOrBlank() || payload.version == null || payload.signature.isNullOrBlank()) {
                    Log.e("QrScanViewModel", "[MOCK] QR Payload format validation failed. Payload: $payload")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid QR Code format.") }
                    return@launch
                }

                // Optional: Check payload.type if needed
                // if (payload.type != "EXPECTED_TYPE") { ... error ... }

                // 3. Mock Signature Validation & Beneficiary Fetch
                // In a real app, send payload to backend here. Backend validates signature & fetches details.
                // We simulate success only if account matches the hardcoded one for now.
                val mockValidAccountId = "BEN-SIM-001" // Corresponds to hardcoded token used before
                val hardcodedTestTokenAccount = "TEST-TOKEN-12345" // Previous placeholder

                // SIMULATE: Check if the ACCOUNT field corresponds to our test token/account
                // This is NOT real signature validation.
                if (payload.account == hardcodedTestTokenAccount || payload.account == mockValidAccountId) {
                    // MOCK SUCCESS: Return predefined beneficiary details
                    val beneficiary = ValidatedBeneficiary(id = mockValidAccountId, name = "Simulated User (Validated)") // Use validated details
                    Log.i("QrScanViewModel", "[MOCK] QR Validation Success. Beneficiary: $beneficiary")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            validatedBeneficiary = beneficiary,
                            validationSuccess = true, // Set flag for navigation trigger
                            errorMessage = null
                        )
                    }
                } else {
                    // MOCK FAILURE: Account doesn't match our test case OR Simulate backend rejecting signature/account
                    Log.e("QrScanViewModel", "[MOCK] QR Validation Failed. Account ID '${payload.account}' not recognized.")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "QR Code not recognized or invalid.") }
                }

            } catch (e: JsonSyntaxException) {
                Log.e("QrScanViewModel", "[MOCK] QR JSON Parsing failed.", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid QR Code data format.") }
            } catch (e: Exception) {
                Log.e("QrScanViewModel", "[MOCK] Error during QR validation.", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error processing QR Code: ${e.message}") }
            }
        }
    }

    // Called by the UI after navigation has been triggered based on validationSuccess
    fun onNavigationHandled() {
        // Reset state related to the completed scan/validation attempt
        _uiState.update {
            it.copy(
                validationSuccess = false,
                validatedBeneficiary = null,
                isLoading = false, // Ensure loading is off
                errorMessage = null // Clear any previous errors
            )
        }
        Log.d("QrScanViewModel", "Navigation handled, resetting validation state.")
    }

    // Kept for explicit error setting if needed (e.g., Camera init error)
    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }
}
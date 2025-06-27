package com.example.merchantapp.viewmodel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.data.QrPayload
import com.example.merchantapp.model.ValidatedBeneficiary // <<< IMPORT FOR THE TOP-LEVEL MODEL CLASS
import com.example.merchantapp.network.ApiService
import com.example.merchantapp.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

// QrScanUiState now uses the imported ValidatedBeneficiary
data class QrScanUiState(
    val isLoading: Boolean = false,
    val amount: String = "0.00", // Amount for the transaction, passed to this screen
    val errorMessage: String? = null,
    val validationSuccess: Boolean = false,
    val validatedBeneficiary: ValidatedBeneficiary? = null // Holds data on successful validation using the imported model
)

class QrScanViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(QrScanUiState())
    val uiState: StateFlow<QrScanUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)

    init {
        val amountArg = savedStateHandle.get<String>("amount") ?: "0.00"
        _uiState.update { it.copy(amount = amountArg) }
        Log.d("QrScanViewModel", "Initialized with amount: $amountArg")
    }

    fun onQrCodeScanned(base64EncodedQrValue: String?) {
        if (base64EncodedQrValue.isNullOrBlank()) {
            Log.w("QrScanViewModel", "QR Scan returned null or blank (Base64) value.")
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid QR Code scanned.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, validationSuccess = false, validatedBeneficiary = null) }

        viewModelScope.launch {
            try {
                Log.d("QrScanViewModel", "Received Base64 Encoded QR: $base64EncodedQrValue")
                val decodedBytes = Base64.decode(base64EncodedQrValue, Base64.DEFAULT)
                val decodedQrTokenJson = String(decodedBytes, StandardCharsets.UTF_8)
                Log.d("QrScanViewModel", "Decoded QR Token (JSON): $decodedQrTokenJson")

                val payload = gson.fromJson(decodedQrTokenJson, QrPayload::class.java)

                if (payload == null || payload.account.isBlank()) {
                    Log.e("QrScanViewModel", "Failed to parse QR JSON into QrPayload or account ID is missing/blank.")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid QR data structure.") }
                    return@launch
                }

                validateWithBackend(payload)

            } catch (e: IllegalArgumentException) {
                Log.e("QrScanViewModel", "Base64 decoding failed.", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid QR Code encoding.") }
            } catch (e: JsonSyntaxException) {
                Log.e("QrScanViewModel", "JSON parsing to QrPayload failed.", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid QR Code data format.") }
            } catch (e: Exception) {
                Log.e("QrScanViewModel", "Error processing scanned QR value before API call.", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error processing QR Code: ${e.message}") }
            }
        }
    }

    private fun validateWithBackend(payload: QrPayload) {
        viewModelScope.launch {
            try {
                Log.i("QrScanViewModel", "Attempting to validate with backend. Account: ${payload.account}")
                val response = apiService.validateQrToken(payload)

                if (response.isSuccessful) {
                    // Explicitly type beneficiaryDetails with the imported ValidatedBeneficiary
                    val beneficiaryDetails: ValidatedBeneficiary? = response.body()
                    if (beneficiaryDetails != null) {
                        Log.i("QrScanViewModel", "Backend Validation Success. Beneficiary ID: ${beneficiaryDetails.accountUuid}, Name: ${beneficiaryDetails.name}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                validatedBeneficiary = beneficiaryDetails, // Uses the imported model instance
                                validationSuccess = true,
                                errorMessage = null
                            )
                        }
                    } else {
                        Log.e("QrScanViewModel", "Backend validation successful but response body was null.")
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Beneficiary data not found from server.") }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("QrScanViewModel", "Backend Validation Failed. Code: ${response.code()}, Body: $errorBody")
                    var apiErrorMessage = "Server error (Code: ${response.code()}). Please try again."
                    if (!errorBody.isNullOrBlank()) {
                        try {
                            val typeToken = object : TypeToken<Map<String, Any>>() {}.type
                            val errorResponseMap: Map<String, Any> = gson.fromJson(errorBody, typeToken)
                            (errorResponseMap["error"] as? String)?.let { backendMsg ->
                                apiErrorMessage = backendMsg
                            }
                        } catch (e: JsonSyntaxException) {
                            Log.w("QrScanViewModel", "Could not parse JSON from error body: $errorBody", e)
                        } catch (e: Exception) {
                            Log.e("QrScanViewModel", "Unexpected error parsing backend error response: $errorBody", e)
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = apiErrorMessage) }
                }
            } catch (e: Exception) {
                Log.e("QrScanViewModel", "Network error during backend validation.", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Network error. Please check connection and try again.") }
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update {
            it.copy(
                validationSuccess = false,
                validatedBeneficiary = null,
                isLoading = false,
                errorMessage = null
            )
        }
        Log.d("QrScanViewModel", "Navigation handled, validation state reset.")
    }

    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }
}
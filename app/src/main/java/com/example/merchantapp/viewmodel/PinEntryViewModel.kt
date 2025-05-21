package com.example.merchantapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.data.ProcessTransactionRequest
import com.example.merchantapp.data.ProcessTransactionResponse // Ensure this data class has 'transactionId: String'
import com.example.merchantapp.network.ApiService
import com.example.merchantapp.network.RetrofitInstance
// Assuming PinEntryUiState is defined elsewhere or you'll add transactionIdForSuccess to it
import com.example.merchantapp.ui.pinentry.PinEntryUiState // Example path
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// If PinEntryUiState is defined in its own file, ensure it has this new field:
// data class PinEntryUiState(
//    // ... your existing fields: amount, beneficiaryId, beneficiaryName, category, pinValue etc...
//    val isLoading: Boolean = false,
//    val errorMessage: String? = null,
//    val isPinVerifiedSuccessfully: Boolean = false, // This means transaction processed successfully
//    val transactionIdForSuccess: String? = null, // <<< NEW FIELD for transaction ID
//    val attemptsRemaining: Int = PinEntryViewModel.MAX_ATTEMPTS,
//    val isLocked: Boolean = false
// )


class PinEntryViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // If PinEntryUiState is defined within this file, add transactionIdForSuccess:
    // data class PinEntryUiState(
    //    val amount: String = "0.00",
    //    val beneficiaryId: String = "",
    //    val beneficiaryName: String = "",
    //    val category: String = "",
    //    val pinValue: String = "",
    //    val isLoading: Boolean = false,
    //    val errorMessage: String? = null,
    //    val isPinVerifiedSuccessfully: Boolean = false,
    //    val transactionIdForSuccess: String? = null, // <<< NEW FIELD
    //    val attemptsRemaining: Int = MAX_ATTEMPTS,
    //    val isLocked: Boolean = false
    // )

    private val _uiState = MutableStateFlow(PinEntryUiState()) // Initialize with default
    val uiState: StateFlow<PinEntryUiState> = _uiState.asStateFlow()

    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)
    private val gson = Gson()

    companion object {
        const val MAX_PIN_LENGTH = 4
        const val MAX_ATTEMPTS = 7
    }

    init {
        val amount = savedStateHandle.get<String>("amount") ?: "0.00"
        val beneficiaryDisplayId = savedStateHandle.get<String>("beneficiaryId") ?: "ERROR_NO_BENEFICIARY_ID"
        val beneficiaryName = savedStateHandle.get<String>("beneficiaryName") ?: "Unknown Beneficiary"
        val category = savedStateHandle.get<String>("category") ?: "Uncategorized"

        _uiState.update {
            // Make sure your PinEntryUiState constructor matches these fields
            it.copy(
                amount = amount,
                beneficiaryId = beneficiaryDisplayId,
                beneficiaryName = beneficiaryName,
                category = category,
                attemptsRemaining = MAX_ATTEMPTS, // Ensure these are part of your UiState
                isLocked = false,                 // Ensure these are part of your UiState
                pinValue = "",
                errorMessage = null,
                isLoading = false,
                isPinVerifiedSuccessfully = false,
                transactionIdForSuccess = null // Initialize new field
            )
        }
        Log.d("PinEntryViewModel", "Initialized with: Amount=$amount, BeneficiaryDisplayID=$beneficiaryDisplayId, Name=$beneficiaryName, Category=$category")
    }

    fun onPinChange(newPinValue: String) {
        if (_uiState.value.isLocked) return

        if (newPinValue.length <= MAX_PIN_LENGTH && newPinValue.all { it.isDigit() }) {
            _uiState.update {
                it.copy(
                    pinValue = newPinValue,
                    errorMessage = null
                )
            }
            if (newPinValue.length == MAX_PIN_LENGTH) {
                submitPinForTransaction()
            }
        }
    }

    fun submitPinForTransaction() {
        if (_uiState.value.isLocked) {
            _uiState.update { it.copy(errorMessage = "PIN entry is locked. Too many attempts.") }
            return
        }
        if (_uiState.value.pinValue.length != MAX_PIN_LENGTH) {
            _uiState.update { it.copy(errorMessage = "PIN must be $MAX_PIN_LENGTH digits.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, transactionIdForSuccess = null) } // Clear previous transactionId

            val currentState = _uiState.value
            val request = ProcessTransactionRequest(
                beneficiaryDisplayId = currentState.beneficiaryId,
                enteredPin = currentState.pinValue,
                amount = currentState.amount,
                description = currentState.category
            )
            Log.d("PinEntryViewModel", "Submitting transaction: BeneficiaryDisplayId=${request.beneficiaryDisplayId}, Amount=${request.amount}, Desc=${request.description}")

            try {
                val response = apiService.processTransaction(request)

                if (response.isSuccessful && response.body() != null) {
                    val transactionResponse = response.body()!! // Should be ProcessTransactionResponse
                    // Ensure ProcessTransactionResponse.kt has 'transactionId: String'
                    Log.i("PinEntryViewModel", "Transaction Processed Successfully. Backend TxID: ${transactionResponse.transactionId}, Status: ${transactionResponse.status}, Msg: ${transactionResponse.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPinVerifiedSuccessfully = true,
                            errorMessage = null, // Clear any prior error
                            // transactionResponse.message could be displayed as a success toast if desired, but not in errorMessage
                            transactionIdForSuccess = transactionResponse.transactionId // <<< STORE THE TRANSACTION ID
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    var apiErrorMessage = "Transaction failed (Code: ${response.code()})"
                    Log.e("PinEntryViewModel", "Backend transaction processing failed. Code: ${response.code()}, Body: $errorBody")

                    if (!errorBody.isNullOrBlank()) {
                        try {
                            val typeToken = object : TypeToken<Map<String, Any>>() {}.type
                            val errorResponseMap: Map<String, Any> = gson.fromJson(errorBody, typeToken)
                            var detailedErrorMsg: String? = null
                            (errorResponseMap["details"] as? Map<String, Any>)?.let { detailsMap ->
                                (detailsMap["message"] as? String)?.let { detailedErrorMsg = it }
                            }
                            if (detailedErrorMsg != null) {
                                apiErrorMessage = detailedErrorMsg!!
                            } else {
                                (errorResponseMap["error"] as? String)?.let { apiErrorMessage = it }
                            }
                        } catch (e: JsonSyntaxException) { /* ... */ } catch (e: Exception) { /* ... */ }
                    }

                    var newAttemptsRemaining = currentState.attemptsRemaining
                    var isNowLocked = currentState.isLocked
                    if (response.code() == 400 && apiErrorMessage.contains("Incorrect PIN", ignoreCase = true)) {
                        newAttemptsRemaining -= 1
                        if (newAttemptsRemaining <= 0) {
                            isNowLocked = true
                            apiErrorMessage = "Incorrect PIN. Entry locked."
                        } else {
                            apiErrorMessage = "Incorrect PIN. $newAttemptsRemaining attempts remaining."
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = apiErrorMessage,
                            pinValue = "",
                            isPinVerifiedSuccessfully = false, // Ensure this is false
                            transactionIdForSuccess = null // Clear on error
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PinEntryViewModel", "Network error or unexpected exception during transaction.", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Network error. Please check connection.",
                        pinValue = "",
                        isPinVerifiedSuccessfully = false, // Ensure this is false
                        transactionIdForSuccess = null // Clear on error
                    )
                }
            }
        }
    }

    fun onSuccessfulNavigationConsumed() {
        _uiState.update {
            it.copy(
                isPinVerifiedSuccessfully = false,
                transactionIdForSuccess = null // Reset the ID after navigation
            )
        }
        Log.d("PinEntryViewModel", "Navigation from PIN screen consumed, transactionIdForSuccess reset.")
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
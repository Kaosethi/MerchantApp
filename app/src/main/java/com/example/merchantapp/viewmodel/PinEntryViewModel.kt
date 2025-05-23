package com.example.merchantapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.data.ProcessTransactionRequest
import com.example.merchantapp.network.ApiService
import com.example.merchantapp.network.RetrofitInstance
import com.example.merchantapp.ui.outcome.OutcomeType // Import the enum
import com.example.merchantapp.ui.pinentry.PinEntryUiState
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class for navigation arguments to TransactionOutcomeScreen
data class OutcomeNavArgs(
    val outcomeType: OutcomeType,
    val title: String,
    val message: String,
    val buttonLabel: String
)

class PinEntryViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PinEntryUiState())
    val uiState: StateFlow<PinEntryUiState> = _uiState.asStateFlow()

    // --- NEW: SharedFlow for one-shot navigation events to TransactionOutcomeScreen ---
    private val _navigateToOutcomeScreenEvent = MutableSharedFlow<OutcomeNavArgs>(replay = 0, extraBufferCapacity = 1)
    val navigateToOutcomeScreenEvent: SharedFlow<OutcomeNavArgs> = _navigateToOutcomeScreenEvent.asSharedFlow()

    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)
    private val gson = Gson()

    companion object {
        const val MAX_PIN_LENGTH = 4
        const val MAX_ATTEMPTS = 7
    }

    init {
        val navArgAmount = savedStateHandle.get<String>("amount")
        val navArgBeneficiaryId = savedStateHandle.get<String>("beneficiaryId")
        val rawNavArgBeneficiaryName = savedStateHandle.get<String>("beneficiaryName")
        val navArgCategory = savedStateHandle.get<String>("category")

        val fixedNavArgBeneficiaryName = rawNavArgBeneficiaryName?.replace('+', ' ')

        Log.d("PinEntryViewModel", "INIT - Received Args: amount=[$navArgAmount], benId=[$navArgBeneficiaryId], benNameRaw=[$rawNavArgBeneficiaryName], benNameFixed=[$fixedNavArgBeneficiaryName], cat=[$navArgCategory]")

        val initialAmount = navArgAmount ?: "0.00"
        val initialBeneficiaryId = navArgBeneficiaryId ?: "ERROR_NO_BENEFICIARY_ID"
        val initialBeneficiaryName = fixedNavArgBeneficiaryName ?: "Unknown Beneficiary"
        val initialCategory = navArgCategory ?: "Uncategorized"

        _uiState.update {
            it.copy(
                amount = initialAmount,
                beneficiaryId = initialBeneficiaryId,
                beneficiaryName = initialBeneficiaryName,
                category = initialCategory,
                attemptsRemaining = MAX_ATTEMPTS,
                isLocked = false,
                pinValue = "",
                errorMessage = null,
                isLoading = false,
                isPinVerifiedSuccessfully = false,
                transactionIdForSuccess = null
            )
        }
        Log.d("PinEntryViewModel", "INIT - uiState initialized: ${_uiState.value}")
    }

    fun onPinChange(newPinValue: String) {
        if (_uiState.value.isLocked) return

        if (newPinValue.length <= MAX_PIN_LENGTH && newPinValue.all { it.isDigit() }) {
            _uiState.update {
                it.copy(pinValue = newPinValue, errorMessage = null)
            }
            if (newPinValue.length == MAX_PIN_LENGTH) {
                submitPinForTransaction()
            }
        }
    }

    fun submitPinForTransaction() {
        if (_uiState.value.isLocked) {
            // If already locked, ensure user is navigated away or shown a clear message.
            // This might be redundant if navigation to OutcomeScreen already occurred.
            _navigateToOutcomeScreenEvent.tryEmit(
                OutcomeNavArgs(
                    outcomeType = OutcomeType.LOCKED_PIN_ENTRY,
                    title = "PIN Entry Locked",
                    message = "Your PIN entry is currently locked due to too many incorrect attempts.",
                    buttonLabel = "Return to Dashboard"
                )
            )
            return
        }
        if (_uiState.value.pinValue.length != MAX_PIN_LENGTH) {
            _uiState.update { it.copy(errorMessage = "PIN must be $MAX_PIN_LENGTH digits.") }
            return
        }

        val currentAmountString = _uiState.value.amount
        val amountAsDouble = currentAmountString.toDoubleOrNull()

        if (amountAsDouble == null || amountAsDouble <= 0.0) {
            Log.e("PinEntryViewModel", "Client-side validation: Amount '$currentAmountString' is not positive.")
            // For client-side validation errors, we might not navigate to full outcome screen,
            // just show an error message on PinEntryScreen.
            _uiState.update {
                it.copy(
                    errorMessage = "Invalid transaction amount. Amount must be greater than zero.",
                    isLoading = false,
                    pinValue = ""
                )
            }
            return
        }

        viewModelScope.launch {
            val pinToSubmit = _uiState.value.pinValue
            val beneficiaryIdForRequest = _uiState.value.beneficiaryId
            val amountForRequest = _uiState.value.amount
            val categoryForRequest = _uiState.value.category

            _uiState.update { it.copy(isLoading = true, errorMessage = null, transactionIdForSuccess = null) }

            val request = ProcessTransactionRequest(
                beneficiaryDisplayId = beneficiaryIdForRequest,
                enteredPin = pinToSubmit,
                amount = amountForRequest,
                description = categoryForRequest
            )
            Log.d("PinEntryViewModel", "API CALL - ProcessTransactionRequest: $request")

            try {
                val response = apiService.processTransaction(request)
                if (response.isSuccessful && response.body() != null) {
                    val transactionResponse = response.body()!!
                    Log.i("PinEntryViewModel", "Transaction Processed Successfully. Backend TxID: ${transactionResponse.transactionId}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPinVerifiedSuccessfully = true,
                            errorMessage = null,
                            transactionIdForSuccess = transactionResponse.transactionId
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    var parsedApiErrorMessage = "Transaction failed (Code: ${response.code()})"
                    Log.e("PinEntryViewModel", "Backend transaction processing failed. Code: ${response.code()}, Body: $errorBody")

                    if (!errorBody.isNullOrBlank()) {
                        try {
                            val typeToken = object : TypeToken<Map<String, Any>>() {}.type
                            val errorResponseMap: Map<String, Any> = gson.fromJson(errorBody, typeToken)
                            (errorResponseMap["error"] as? String)?.let { parsedApiErrorMessage = it }
                        } catch (e: Exception) { /* ... logging ... */ }
                    }

                    // --- MODIFIED ERROR HANDLING TO USE TransactionOutcomeScreen ---
                    if (response.code() == 400 && parsedApiErrorMessage.contains("Incorrect PIN", ignoreCase = true)) {
                        Log.d("PinEntryViewModel", "Incorrect PIN detected. Parsed error: $parsedApiErrorMessage")
                        val currentAttempts = _uiState.value.attemptsRemaining
                        val newAttempts = currentAttempts - 1
                        Log.d("PinEntryViewModel", "Updating attempts. Old: $currentAttempts, New: $newAttempts")

                        if (newAttempts <= 0) {
                            Log.d("PinEntryViewModel", "PIN entry locked. Emitting navigateToOutcomeScreenEvent.")
                            _navigateToOutcomeScreenEvent.tryEmit(
                                OutcomeNavArgs(
                                    outcomeType = OutcomeType.LOCKED_PIN_ENTRY,
                                    title = "PIN Entry Locked",
                                    message = "For security, PIN entry has been locked due to too many incorrect attempts. Please contact support if this issue persists.",
                                    buttonLabel = "Return to Dashboard"
                                )
                            )
                            _uiState.update { // Still update local state, though navigation will occur
                                it.copy(
                                    isLoading = false,
                                    pinValue = "",
                                    attemptsRemaining = 0,
                                    isLocked = true,
                                    errorMessage = null // Error is shown on OutcomeScreen
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Incorrect PIN. $newAttempts attempts remaining.", // Show attempts on PinEntryScreen
                                    pinValue = "",
                                    attemptsRemaining = newAttempts,
                                    isLocked = false
                                )
                            }
                        }
                    } else if (response.code() == 400 && parsedApiErrorMessage.contains("Insufficient Funds", ignoreCase = true)) {
                        Log.d("PinEntryViewModel", "Insufficient funds detected. Emitting navigateToOutcomeScreenEvent.")
                        _navigateToOutcomeScreenEvent.tryEmit(
                            OutcomeNavArgs(
                                outcomeType = OutcomeType.DECLINED_INSUFFICIENT_FUNDS,
                                title = "Transaction Declined",
                                message = parsedApiErrorMessage, // Use the message from backend
                                buttonLabel = "OK"
                            )
                        )
                        _uiState.update { it.copy(isLoading = false, pinValue = "", errorMessage = null) }
                    }
                    // Add more 'else if' blocks here for other specific declined reasons from backend
                    // e.g., else if (response.code() == 403 && parsedApiErrorMessage.contains("Account Suspended", ignoreCase = true)) ...
                    else {
                        // General backend error not specifically handled above
                        Log.d("PinEntryViewModel", "General backend error. Emitting navigateToOutcomeScreenEvent.")
                        _navigateToOutcomeScreenEvent.tryEmit(
                            OutcomeNavArgs(
                                outcomeType = OutcomeType.DECLINED_GENERAL, // Or ERROR_GENERAL if more appropriate
                                title = "Transaction Failed",
                                message = parsedApiErrorMessage,
                                buttonLabel = "Return to Dashboard"
                            )
                        )
                        _uiState.update { it.copy(isLoading = false, pinValue = "", errorMessage = null) }
                    }
                }
            } catch (e: Exception) { // Network errors, etc.
                Log.e("PinEntryViewModel", "Network error or unexpected exception during transaction.", e)
                _navigateToOutcomeScreenEvent.tryEmit(
                    OutcomeNavArgs(
                        outcomeType = OutcomeType.ERROR_NETWORK,
                        title = "Network Error",
                        message = "Could not connect to the server. Please check your internet connection and try again.",
                        buttonLabel = "OK"
                    )
                )
                _uiState.update {
                    it.copy(isLoading = false, pinValue = "", errorMessage = null)
                }
            }
        }
    }

    fun onSuccessfulNavigationConsumed() {
        _uiState.update {
            it.copy(isPinVerifiedSuccessfully = false, transactionIdForSuccess = null)
        }
        Log.d("PinEntryViewModel", "Successful navigation to SuccessScreen consumed.")
    }

    // --- NEW: Call this if UI consumes the outcome navigation event ---
    // Not strictly necessary with SharedFlow if consumption is handled by navigation itself,
    // but can be useful for logging or complex scenarios.
    fun onOutcomeNavigationObserved() {
        Log.d("PinEntryViewModel", "Outcome navigation event observed by UI.")
    }

    fun clearErrorMessage() { // Used if errors are shown directly on PinEntryScreen
        _uiState.update { it.copy(errorMessage = null) }
    }
}
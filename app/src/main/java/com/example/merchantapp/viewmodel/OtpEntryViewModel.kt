// File: app/src/main/java/com/example/merchantapp/viewmodel/OtpEntryViewModel.kt
package com.example.merchantapp.viewmodel

import android.app.Application // <-- NEW IMPORT
import android.util.Log
import androidx.lifecycle.AndroidViewModel // <-- NEW IMPORT instead of ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.model.VerifyOtpRequest // <-- NEW IMPORT
import com.example.merchantapp.model.ForgotPasswordRequest // For resend OTP
import com.example.merchantapp.network.ApiService // <-- NEW IMPORT
import com.example.merchantapp.network.RetrofitInstance // <-- NEW IMPORT
import com.example.merchantapp.ui.otp.OtpEntryUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// const val MOCK_OTP = "111111" // No longer needed for verification
const val OTP_LENGTH = 6
const val RESEND_OTP_TIMER_SECONDS = 60

class OtpEntryViewModel(
    application: Application, // <-- NEW: For AndroidViewModel
    private val savedStateHandle: SavedStateHandle // Keep SavedStateHandle
) : AndroidViewModel(application) { // <-- EXTEND AndroidViewModel

    private val _email: String = savedStateHandle.get<String>("email")
        ?: run {
            Log.e("OtpEntryVM", "Critical: Email not found in SavedStateHandle!")
            "error_email_not_passed" // Fallback, should ideally not happen
        }

    private val _uiState = MutableStateFlow(OtpEntryUiState(email = _email))
    val uiState: StateFlow<OtpEntryUiState> = _uiState.asStateFlow()

    private var resendOtpTimerJob: Job? = null

    // Access your ApiService instance
    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)

    init {
        Log.d("OtpEntryVM", "ViewModel initialized for email: $_email")
        if (_email == "error_email_not_passed") {
            _uiState.update { it.copy(errorMessage = "Could not load email. Please go back.") }
        } else {
            startResendOtpTimer()
        }
    }

    fun onOtpChange(newOtpValue: String) {
        if (newOtpValue.length <= OTP_LENGTH && newOtpValue.all { it.isDigit() }) {
            _uiState.update { currentState ->
                currentState.copy(
                    otpValue = newOtpValue,
                    errorMessage = null
                )
            }
            if (newOtpValue.length == OTP_LENGTH) {
                verifyOtp() // Automatically verify when OTP is full
            }
        }
    }

    fun verifyOtp() {
        val currentOtp = _uiState.value.otpValue
        Log.d("OtpEntryVM", "Verifying OTP: $currentOtp for email: $_email via API")

        if (_email == "error_email_not_passed") {
            _uiState.update { it.copy(errorMessage = "Cannot verify OTP: Email not available.", isLoading = false) }
            return
        }

        if (currentOtp.length != OTP_LENGTH) {
            _uiState.update { it.copy(errorMessage = "OTP must be $OTP_LENGTH digits.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, apiMessage = null) }

        viewModelScope.launch {
            try {
                val request = VerifyOtpRequest(email = _email, otp = currentOtp)
                val response = apiService.verifyOtp(request)

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Log.d("OtpEntryVM", "OTP Verification API success: ${responseBody.message}")
                    if (responseBody.resetAuthorizationToken != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isOtpVerified = true, // Signal successful verification
                                resetAuthToken = responseBody.resetAuthorizationToken, // Store the token
                                apiMessage = responseBody.message,
                                errorMessage = null
                            )
                        }
                    } else {
                        // Should not happen if API guarantees token on 200 for verify success
                        Log.e("OtpEntryVM", "OTP Verification API success but no reset token found.")
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = responseBody.message ?: "Verification successful but token missing.")
                        }
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: "Unknown server error"
                    Log.e("OtpEntryVM", "OTP Verification API error: ${response.code()} - $errorBodyString")
                    // Attempt to parse error if backend sends structured error
                    val errorMessage = try {
                        // Assuming error response is like { "message": "error details" }
                        val errorResponse = com.google.gson.Gson().fromJson(errorBodyString, com.example.merchantapp.model.ForgotPasswordResponse::class.java) // Reusing for simple message
                        errorResponse.message
                    } catch (e: Exception) {
                        "Invalid OTP or an error occurred (Code: ${response.code()})."
                    }
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isOtpVerified = false,
                            errorMessage = errorMessage
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e("OtpEntryVM", "Network error during OTP verification: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Network error. Please check connection.")
                }
            } catch (e: Exception) {
                Log.e("OtpEntryVM", "Error during OTP verification: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "An unexpected error occurred.")
                }
            }
        }
    }

    fun resetOtpVerifiedStateAndToken() { // Also clear token
        if (_uiState.value.isOtpVerified) {
            _uiState.update { it.copy(isOtpVerified = false, resetAuthToken = null, apiMessage = null) }
            Log.d("OtpEntryVM", "isOtpVerified state and resetAuthToken reset.")
        }
    }

    fun resendOtp() {
        if (!_uiState.value.isResendEnabled || _email == "error_email_not_passed") {
            Log.w("OtpEntryVM", "Resend OTP called when not enabled or email is missing.")
            return
        }

        Log.d("OtpEntryVM", "Resending OTP for email: $_email via API")
        _uiState.update { it.copy(isLoading = true, errorMessage = null, apiMessage = null, isResendEnabled = false) }

        viewModelScope.launch {
            try {
                // Call the requestPasswordReset endpoint again for resending
                val request = ForgotPasswordRequest(email = _email)
                val response = apiService.requestPasswordReset(request) // Reusing the first endpoint

                if (response.isSuccessful && response.body() != null) {
                    Log.d("OtpEntryVM", "Resend OTP API success: ${response.body()!!.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpValue = "", // Clear current OTP field
                            // apiMessage = response.body()!!.message // Or a custom "New OTP sent"
                            apiMessage = "A new OTP has been sent to your email.",
                            errorMessage = null
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("OtpEntryVM", "Resend OTP API error: ${response.code()} - $errorBody")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Failed to resend OTP (Error: ${response.code()}).")
                    }
                }
            } catch (e: IOException) {
                Log.e("OtpEntryVM", "Network error during resend OTP: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Network error during resend.") }
            } catch (e: Exception) {
                Log.e("OtpEntryVM", "Error during resend OTP: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Unexpected error during resend.") }
            } finally {
                startResendOtpTimer() // Restart timer regardless of outcome, but after state update
            }
        }
    }

    private fun startResendOtpTimer() {
        // ... (timer logic remains the same)
        resendOtpTimerJob?.cancel()
        _uiState.update { it.copy(isResendEnabled = false, resendTimerSeconds = RESEND_OTP_TIMER_SECONDS) }
        Log.d("OtpEntryVM", "Resend OTP timer started for $RESEND_OTP_TIMER_SECONDS seconds.")

        resendOtpTimerJob = viewModelScope.launch {
            for (i in RESEND_OTP_TIMER_SECONDS downTo 1) {
                _uiState.update { it.copy(resendTimerSeconds = i) }
                delay(1000)
            }
            _uiState.update { it.copy(isResendEnabled = true, resendTimerSeconds = 0) }
            Log.d("OtpEntryVM", "Resend OTP timer finished. Resend enabled.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        resendOtpTimerJob?.cancel()
        Log.d("OtpEntryVM", "ViewModel cleared, timer cancelled.")
    }
}
// File: app/src/main/java/com/example/merchantapp/viewmodel/OtpEntryViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.ui.otp.OtpEntryUiState // Correct import for UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MOCK_OTP = "111111" // Our mock OTP
const val OTP_LENGTH = 6
const val RESEND_OTP_TIMER_SECONDS = 60 // Standard 60 seconds

class OtpEntryViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Extract email from navigation arguments (passed by NavHost)
    private val _email: String = savedStateHandle.get<String>("email") ?: "Error: Email not found"

    private val _uiState = MutableStateFlow(OtpEntryUiState(email = _email))
    val uiState: StateFlow<OtpEntryUiState> = _uiState.asStateFlow()

    private var resendOtpTimerJob: Job? = null

    init {
        Log.d("OtpEntryVM", "ViewModel initialized for email: $_email")
        startResendOtpTimer()
    }

    /**
     * Updates the OTP value in the UI state as the user types.
     * Ensures only digits are accepted and max length is OTP_LENGTH.
     */
    fun onOtpChange(newOtpValue: String) {
        if (newOtpValue.length <= OTP_LENGTH && newOtpValue.all { it.isDigit() }) {
            _uiState.update { currentState ->
                currentState.copy(
                    otpValue = newOtpValue,
                    errorMessage = null // Clear error on new input
                )
            }
            // If OTP reaches full length, automatically attempt verification
            if (newOtpValue.length == OTP_LENGTH) {
                verifyOtp()
            }
        }
    }

    /**
     * Verifies the entered OTP against the mock OTP.
     */
    fun verifyOtp() {
        val currentOtp = _uiState.value.otpValue
        Log.d("OtpEntryVM", "Verifying OTP: $currentOtp for email: $_email")

        if (currentOtp.length != OTP_LENGTH) {
            _uiState.update { it.copy(errorMessage = "OTP must be $OTP_LENGTH digits.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(1000) // Simulate network delay for verification

            if (currentOtp == MOCK_OTP) {
                Log.d("OtpEntryVM", "OTP Verified successfully.")
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isOtpVerified = true, // Signal successful verification
                        errorMessage = null
                    )
                }
            } else {
                Log.w("OtpEntryVM", "OTP Verification failed. Entered: $currentOtp, Expected: $MOCK_OTP")
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isOtpVerified = false,
                        errorMessage = "Invalid OTP. Please try again."
                        // Optionally clear OTP field: otpValue = ""
                    )
                }
            }
        }
    }

    /**
     * Resets the OTP verification success flag.
     * Called by UI after navigation to the next screen.
     */
    fun resetOtpVerifiedState() {
        if (_uiState.value.isOtpVerified) {
            _uiState.update { it.copy(isOtpVerified = false) }
            Log.d("OtpEntryVM", "isOtpVerified state reset.")
        }
    }

    /**
     * Simulates resending the OTP and restarts the timer.
     */
    fun resendOtp() {
        if (!_uiState.value.isResendEnabled) return // Should not be callable if timer is active

        Log.d("OtpEntryVM", "Resending OTP for email: $_email")
        _uiState.update { it.copy(isLoading = true, errorMessage = null, isResendEnabled = false) } // Show loading, disable resend

        viewModelScope.launch {
            delay(1000) // Simulate network delay for resending OTP
            // In a real app, call backend to resend OTP
            Log.d("OtpEntryVM", "Mock OTP resent successfully to $_email.")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    otpValue = "", // Clear current OTP field
                    errorMessage = "New OTP 'sent'. Please check your email." // Inform user
                )
            }
            startResendOtpTimer() // Restart timer
        }
    }

    /**
     * Starts or restarts the countdown timer for enabling the "Resend OTP" button.
     */
    private fun startResendOtpTimer() {
        resendOtpTimerJob?.cancel() // Cancel any existing timer job
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
        resendOtpTimerJob?.cancel() // Ensure timer is cancelled when ViewModel is cleared
        Log.d("OtpEntryVM", "ViewModel cleared, timer cancelled.")
    }
}
// File: app/src/main/java/com/example/merchantapp/viewmodel/ForgotPasswordViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import android.util.Patterns // For basic email validation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.ui.forgotpassword.ForgotPasswordUiState // Ensure this import is correct
import kotlinx.coroutines.delay // For simulating network delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = newEmail,
                errorMessage = null // Clear error when user types
            )
        }
        Log.d("ForgotPasswordVM", "Email updated: $newEmail")
    }

    fun submitRequest() { // Renaming this to reflect sending OTP might be good later, e.g., sendOtpRequest
        val currentEmail = _uiState.value.email.trim()

        if (currentEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email cannot be empty.") }
            Log.w("ForgotPasswordVM", "Submit failed: Email empty")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email format.") }
            Log.w("ForgotPasswordVM", "Submit failed: Invalid email format - $currentEmail")
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        Log.d("ForgotPasswordVM", "Requesting OTP for: $currentEmail")

        viewModelScope.launch {
            try {
                delay(1500) // Simulate network delay for sending OTP

                // --- Mock Logic for sending OTP ---
                // In a real app, call backend to send OTP to 'currentEmail'
                // Backend would confirm if email is registered.
                // For now, assume success if email format is valid.
                Log.d("ForgotPasswordVM", "Mock OTP 'sent' successfully to $currentEmail.")
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isSuccess = true, // Signal that OTP sending was "successful"
                        errorMessage = null
                    )
                }
                // --- End Mock Logic ---

            } catch (e: Exception) {
                Log.e("ForgotPasswordVM", "Error during mock OTP sending: ${e.message}")
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = "An unexpected error occurred while sending OTP."
                    )
                }
            }
        }
    }

    fun resetSuccessState() {
        if (_uiState.value.isSuccess) {
            _uiState.update { it.copy(isSuccess = false) }
            Log.d("ForgotPasswordVM", "Success state (for OTP sent) reset.")
        }
    }
}
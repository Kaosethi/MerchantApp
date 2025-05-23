// File: app/src/main/java/com/example/merchantapp/viewmodel/ForgotPasswordViewModel.kt
package com.example.merchantapp.viewmodel

import android.app.Application // <-- NEW IMPORT
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel // <-- NEW IMPORT instead of ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.model.ForgotPasswordRequest
import com.example.merchantapp.network.ApiService
import com.example.merchantapp.network.RetrofitInstance // <-- Correct import for your RetrofitInstance
import com.example.merchantapp.ui.forgotpassword.ForgotPasswordUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// ViewModel now extends AndroidViewModel and takes Application in constructor
class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    // Get ApiService using application context from AndroidViewModel
    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)

    fun onEmailChange(newEmail: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = newEmail,
                errorMessage = null,
                apiMessage = null
            )
        }
        Log.d("ForgotPasswordVM", "Email updated: $newEmail")
    }

    fun submitRequestOtp() {
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

        _uiState.update { it.copy(isLoading = true, errorMessage = null, apiMessage = null) }
        Log.d("ForgotPasswordVM", "Requesting OTP for: $currentEmail via API")

        viewModelScope.launch {
            try {
                val request = ForgotPasswordRequest(email = currentEmail)
                val response = apiService.requestPasswordReset(request) // Use the initialized apiService

                if (response.isSuccessful && response.body() != null) {
                    Log.d("ForgotPasswordVM", "OTP request API success: ${response.body()!!.message}")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isOtpRequestSuccess = true,
                            apiMessage = response.body()!!.message,
                            errorMessage = null
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error from server"
                    Log.e("ForgotPasswordVM", "OTP request API error: ${response.code()} - $errorBody")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isOtpRequestSuccess = false,
                            errorMessage = "Failed to request OTP (Code: ${response.code()}). Please try again."
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e("ForgotPasswordVM", "Network error during OTP request: ${e.message}", e)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isOtpRequestSuccess = false,
                        errorMessage = "Network error. Please check your connection."
                    )
                }
            } catch (e: Exception) {
                Log.e("ForgotPasswordVM", "Error during OTP request: ${e.message}", e)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isOtpRequestSuccess = false,
                        errorMessage = "An unexpected error occurred. Please try again."
                    )
                }
            }
        }
    }

    fun resetOtpRequestSuccessState() {
        if (_uiState.value.isOtpRequestSuccess) {
            _uiState.update { it.copy(isOtpRequestSuccess = false, apiMessage = null) }
            Log.d("ForgotPasswordVM", "isOtpRequestSuccess state reset.")
        }
    }
}
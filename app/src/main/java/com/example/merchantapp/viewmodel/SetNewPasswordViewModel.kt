// File: app/src/main/java/com/example/merchantapp/viewmodel/SetNewPasswordViewModel.kt
package com.example.merchantapp.viewmodel

import android.app.Application // <-- NEW IMPORT
import android.util.Log
import androidx.lifecycle.AndroidViewModel // <-- NEW IMPORT
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.model.ResetPasswordRequest // <-- NEW IMPORT
import com.example.merchantapp.network.ApiService // <-- NEW IMPORT
import com.example.merchantapp.network.RetrofitInstance // <-- NEW IMPORT
import com.example.merchantapp.ui.setnewpassword.SetNewPasswordUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class SetNewPasswordViewModel(
    application: Application, // <-- NEW
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) { // <-- EXTEND AndroidViewModel

    private val _uiState = MutableStateFlow(SetNewPasswordUiState())
    val uiState: StateFlow<SetNewPasswordUiState> = _uiState.asStateFlow()

    // Retrieve resetAuthorizationToken from navigation arguments
    // Ensure your navigation passes this argument with the key "resetAuthToken"
    private val resetAuthToken: String? = savedStateHandle.get<String>("resetAuthToken")

    // Access your ApiService instance
    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)

    init {
        if (resetAuthToken == null) {
            Log.e("SetNewPasswordVM", "Critical: resetAuthToken not found in SavedStateHandle!")
            _uiState.update { it.copy(errorMessage = "Session invalid. Please start over.", canSubmit = false) }
        }
        // You might also pass and display the email for user context, but it's not needed for the API call
        // val email: String? = savedStateHandle.get<String>("email")
        // _uiState.update { it.copy(emailForDisplay = email) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update {
            val passwordsMatch = password.isNotEmpty() && password == it.confirmPassword
            it.copy(
                newPassword = password,
                errorMessage = null,
                apiMessage = null,
                passwordsMatch = passwordsMatch,
                canSubmit = passwordsMatch && password.length >= 8 // Basic validation for enabling submit
            )
        }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update {
            val passwordsMatch = password.isNotEmpty() && it.newPassword == password
            it.copy(
                confirmPassword = password,
                errorMessage = null,
                apiMessage = null,
                passwordsMatch = passwordsMatch,
                canSubmit = passwordsMatch && it.newPassword.length >= 8 // Basic validation for enabling submit
            )
        }
    }

    fun setNewPassword() {
        val currentToken = resetAuthToken
        if (currentToken == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Password reset session is invalid. Please start over.") }
            Log.e("SetNewPasswordVM", "Attempted to set new password without a reset token.")
            return
        }

        val currentState = _uiState.value
        val newPassword = currentState.newPassword
        val confirmPassword = currentState.confirmPassword

        // Client-side validations
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Password fields cannot be empty.", canSubmit = false) }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Passwords do not match.", passwordsMatch = false, canSubmit = false) }
            return
        }
        if (newPassword.length < 8) { // Match backend Zod validation
            _uiState.update { it.copy(isLoading = false, errorMessage = "Password must be at least 8 characters long.", canSubmit = false) }
            return
        }
        // Add more complex client-side password validation regex if desired, to match backend potentially

        _uiState.update { it.copy(isLoading = true, errorMessage = null, apiMessage = null) }
        Log.d("SetNewPasswordVM", "Attempting to set new password via API.")

        viewModelScope.launch {
            try {
                val request = ResetPasswordRequest(
                    resetAuthorizationToken = currentToken,
                    newPassword = newPassword
                )
                val response = apiService.resetPassword(request)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("SetNewPasswordVM", "Set new password API success: ${response.body()!!.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPasswordSetSuccessfully = true,
                            apiMessage = response.body()!!.message,
                            errorMessage = null
                        )
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: "Unknown server error"
                    Log.e("SetNewPasswordVM", "Set new password API error: ${response.code()} - $errorBodyString")
                    val errorMessage = try {
                        val errorResponse = com.google.gson.Gson().fromJson(errorBodyString, com.example.merchantapp.model.ResetPasswordResponse::class.java)
                        errorResponse.message // Or a more specific error from backend if available
                    } catch (e: Exception) {
                        "Failed to set new password (Code: ${response.code()}). Please try again."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPasswordSetSuccessfully = false,
                            errorMessage = errorMessage
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e("SetNewPasswordVM", "Network error during set new password: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Network error. Please check connection.") }
            } catch (e: Exception) {
                Log.e("SetNewPasswordVM", "Error during set new password: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "An unexpected error occurred.") }
            }
        }
    }

    fun onPasswordSetSuccessNavigationConsumed() {
        _uiState.update { it.copy(isPasswordSetSuccessfully = false, apiMessage = null) }
        Log.d("SetNewPasswordVM", "isPasswordSetSuccessfully state reset.")
    }
}
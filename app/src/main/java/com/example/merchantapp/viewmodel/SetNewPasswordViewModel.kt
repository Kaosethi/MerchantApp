// SetNewPasswordViewModel.kt
package com.example.merchantapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.ui.setnewpassword.SetNewPasswordUiState // Ensure this import path matches your UiState location
import kotlinx.coroutines.delay // For mocking API call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ADDED: Entire new file
class SetNewPasswordViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetNewPasswordUiState())
    val uiState: StateFlow<SetNewPasswordUiState> = _uiState.asStateFlow()

    // Retrieve email/token from navigation arguments
    private val emailOrToken: String = savedStateHandle.get<String>("email_or_token") ?: ""

    init {
        _uiState.update { it.copy(emailOrToken = this.emailOrToken) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                errorMessage = null, // Clear error on input change
                passwordsMatch = password == it.confirmPassword // Check match on change
            )
        }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                confirmPassword = password,
                errorMessage = null, // Clear error on input change
                passwordsMatch = it.newPassword == password // Check match on change
            )
        }
    }

    fun setNewPassword() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentState = _uiState.value
            val newPassword = currentState.newPassword
            val confirmPassword = currentState.confirmPassword

            // Basic Validations
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Password fields cannot be empty."
                    )
                }
                return@launch
            }

            if (newPassword != confirmPassword) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Passwords do not match.",
                        passwordsMatch = false
                    )
                }
                return@launch
            }

            // Mock Password Complexity (adjust as needed)
            // For simplicity, we'll just check length for now.
            // The uiState.passwordRequirementsMessage can guide the user.
            if (newPassword.length < 8) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Password must be at least 8 characters long."
                    )
                }
                return@launch
            }
            // Add more complex regex checks here if needed, e.g.,
            // val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
            // if (!newPassword.matches(passwordPattern)) { /* set error */ return@launch }


            // Simulate API call
            delay(1500) // Simulate network latency

            // Mock success scenario
            val mockApiSuccess = true // Change to false to test error handling

            if (mockApiSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPasswordSetSuccessfully = true
                    )
                }
            } else {
                // Mock failure scenario
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to set new password. Please try again."
                    )
                }
            }
        }
    }

    fun onPasswordSetSuccessNavigationConsumed() {
        _uiState.update { it.copy(isPasswordSetSuccessfully = false) }
    }
}
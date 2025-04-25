// File: app/src/main/java/com/example/merchantapp/viewmodel/RegisterViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.R // ADDED: Import R for string resource IDs
import kotlinx.coroutines.delay // For simulating network delay later
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ADDED: Enum to identify fields for validation state
enum class RegisterFormField {
    STORE_NAME,
    CONTACT_NAME,
    EMAIL,
    PHONE,
    ADDRESS,
    PASSWORD,
    CONFIRM_PASSWORD
}

// MODIFIED: RegisterUiState to include validation fields
data class RegisterUiState(
    val storeName: String = "",
    val contactName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val registrationError: Int? = null, // Use Int for String Resource ID
    val registrationSuccess: Boolean = false,
    val invalidFields: Set<RegisterFormField> = emptySet(), // Track invalid fields
    val showGeneralValidationError: Boolean = false // Flag for general message
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Minimum password length requirement
    private val minPasswordLength = 6

    // --- Update functions for each field ---
    fun onStoreNameChange(newValue: String) {
        _uiState.update { it.copy(storeName = newValue, invalidFields = it.invalidFields - RegisterFormField.STORE_NAME) }
    }

    fun onContactNameChange(newValue: String) {
        _uiState.update { it.copy(contactName = newValue, invalidFields = it.invalidFields - RegisterFormField.CONTACT_NAME) }
    }

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue, invalidFields = it.invalidFields - RegisterFormField.EMAIL) }
    }

    fun onPhoneChange(newValue: String) {
        _uiState.update { it.copy(phone = newValue, invalidFields = it.invalidFields - RegisterFormField.PHONE) }
    }

    fun onAddressChange(newValue: String) {
        _uiState.update { it.copy(address = newValue, invalidFields = it.invalidFields - RegisterFormField.ADDRESS) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue, invalidFields = it.invalidFields - RegisterFormField.PASSWORD) }
    }

    fun onConfirmPasswordChange(newValue: String) {
        _uiState.update { it.copy(confirmPassword = newValue, invalidFields = it.invalidFields - RegisterFormField.CONFIRM_PASSWORD) }
    }

    // --- Submit Action ---
    fun onSubmitClick() {
        Log.d("RegisterViewModel", "Submit Clicked - Starting Validation")
        // Clear previous validation errors first
        _uiState.update { it.copy(registrationError = null, showGeneralValidationError = false, invalidFields = emptySet()) }

        val currentState = uiState.value // Get current state for validation
        val validationErrors = mutableSetOf<RegisterFormField>()

        // 1. Check for empty fields
        if (currentState.storeName.isBlank()) validationErrors.add(RegisterFormField.STORE_NAME)
        if (currentState.contactName.isBlank()) validationErrors.add(RegisterFormField.CONTACT_NAME)
        if (currentState.email.isBlank()) validationErrors.add(RegisterFormField.EMAIL)
        if (currentState.phone.isBlank()) validationErrors.add(RegisterFormField.PHONE) // Assuming phone is required
        if (currentState.address.isBlank()) validationErrors.add(RegisterFormField.ADDRESS)
        if (currentState.password.isBlank()) validationErrors.add(RegisterFormField.PASSWORD)
        if (currentState.confirmPassword.isBlank()) validationErrors.add(RegisterFormField.CONFIRM_PASSWORD)

        // If any field is empty, show general message and mark fields
        if (validationErrors.isNotEmpty()) {
            Log.d("RegisterViewModel", "Validation Failed: Empty fields found - ${validationErrors.joinToString()}")
            _uiState.update {
                it.copy(
                    invalidFields = validationErrors,
                    showGeneralValidationError = true // Show the general message
                )
            }
            return // Stop processing
        }

        // 2. Check password requirements (only if not blank)
        if (currentState.password.length < minPasswordLength) {
            Log.d("RegisterViewModel", "Validation Failed: Password too short")
            _uiState.update { it.copy(registrationError = R.string.validation_password_too_short) }
            return // Stop processing
        }

        // 3. Check if passwords match (only if not blank)
        if (currentState.password != currentState.confirmPassword) {
            Log.d("RegisterViewModel", "Validation Failed: Passwords do not match")
            _uiState.update { it.copy(registrationError = R.string.validation_passwords_mismatch) }
            return // Stop processing
        }

        // --- Validation Passed ---
        Log.d("RegisterViewModel", "Validation Passed. Proceeding with registration (Mocked).")
        Log.d("RegisterViewModel", "Store Name: ${currentState.storeName}")
        Log.d("RegisterViewModel", "Contact Name: ${currentState.contactName}")
        // ... log other fields if needed (avoid passwords in production) ...

        // --- Mocked API Call ---
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                delay(1500) // Simulate network call
                // TODO: Replace with actual repository.register(...) call
                Log.d("RegisterViewModel", "Mock registration successful")
                _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Mock registration failed", e)
                _uiState.update { it.copy(isLoading = false, registrationError = R.string.registration_failed) } // Generic failure message
            }
        }
    }

    // Function to clear specific errors after they've been shown (e.g., in a Toast)
    fun clearError() {
        _uiState.update { it.copy(registrationError = null) }
    }

    // Function to reset success flag after navigation or message shown
    fun consumeSuccess() {
        _uiState.update { it.copy(registrationSuccess = false) }
    }
}
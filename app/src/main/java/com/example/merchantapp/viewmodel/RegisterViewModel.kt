// File: app/src/main/java/com/example/merchantapp/viewmodel/RegisterViewModel.kt
package com.example.merchantapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.R // Ensure this is imported for R.string resources
import com.example.merchantapp.data.repository.MerchantRepository
import com.example.merchantapp.data.repository.Result
import com.example.merchantapp.model.MerchantRegistrationRequest // CORRECTED: Ensure this import is present and valid
// import kotlinx.coroutines.delay // Can remove if not using delay for simulation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterFormField {
    STORE_NAME,
    CONTACT_NAME,
    EMAIL,
    PHONE,
    ADDRESS,
    PASSWORD,
    CONFIRM_PASSWORD
}

data class RegisterUiState(
    val storeName: String = "",
    val contactName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val registrationError: Int? = null,
    val registrationErrorMessage: String? = null,
    val registrationSuccess: Boolean = false,
    val invalidFields: Set<RegisterFormField> = emptySet(),
    val showGeneralValidationError: Boolean = false
)

class RegisterViewModel(
    private val merchantRepository: MerchantRepository = MerchantRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val minPasswordLength = 6

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
        val sanitizedPhone = newValue.filter { it.isDigit() }
        _uiState.update { it.copy(phone = sanitizedPhone, invalidFields = it.invalidFields - RegisterFormField.PHONE) }
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

    fun onSubmitClick() {
        Log.d("RegisterViewModel", "Submit Clicked - Starting Validation")
        _uiState.update { it.copy(registrationError = null, registrationErrorMessage = null, showGeneralValidationError = false, invalidFields = emptySet()) }

        val currentState = _uiState.value
        val validationErrors = mutableSetOf<RegisterFormField>()

        if (currentState.storeName.isBlank()) validationErrors.add(RegisterFormField.STORE_NAME)
        if (currentState.contactName.isBlank()) validationErrors.add(RegisterFormField.CONTACT_NAME)
        if (currentState.email.isBlank()) validationErrors.add(RegisterFormField.EMAIL)
        if (currentState.address.isBlank()) validationErrors.add(RegisterFormField.ADDRESS)
        if (currentState.password.isBlank()) validationErrors.add(RegisterFormField.PASSWORD)
        if (currentState.confirmPassword.isBlank()) validationErrors.add(RegisterFormField.CONFIRM_PASSWORD)
        if (currentState.phone.isBlank()) {
            validationErrors.add(RegisterFormField.PHONE)
        }

        if (validationErrors.isNotEmpty()) {
            Log.d("RegisterViewModel", "Validation Failed: Empty/invalid fields - ${validationErrors.joinToString()}")
            _uiState.update {
                it.copy(
                    invalidFields = validationErrors,
                    showGeneralValidationError = true
                )
            }
            return
        }

        if (currentState.password.length < minPasswordLength) {
            Log.d("RegisterViewModel", "Validation Failed: Password too short")
            _uiState.update { it.copy(registrationError = R.string.validation_password_too_short, invalidFields = it.invalidFields + RegisterFormField.PASSWORD) }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            Log.d("RegisterViewModel", "Validation Failed: Passwords do not match")
            _uiState.update { it.copy(registrationError = R.string.validation_passwords_mismatch, invalidFields = it.invalidFields + RegisterFormField.CONFIRM_PASSWORD + RegisterFormField.PASSWORD) }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            Log.d("RegisterViewModel", "Validation Failed: Invalid email format")
            // Ensure R.string.validation_email_invalid exists
            _uiState.update { it.copy(registrationError = R.string.validation_email_invalid, invalidFields = it.invalidFields + RegisterFormField.EMAIL) }
            return
        }

        Log.d("RegisterViewModel", "Validation Passed. Proceeding with actual registration.")
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val registrationRequest = MerchantRegistrationRequest(
                name = currentState.contactName,
                email = currentState.email,
                password = currentState.password,
                location = currentState.address,
                category = currentState.storeName,
                contactEmail = currentState.email
            )

            Log.d("RegisterViewModel", "Registration Request: $registrationRequest")

            when (val result = merchantRepository.registerMerchant(registrationRequest)) {
                is Result.Success -> {
                    Log.i("RegisterViewModel", "Registration API call successful: ${result.data.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrationSuccess = true,
                        )
                    }
                }
                is Result.Error -> {
                    Log.e("RegisterViewModel", "Registration API call failed: ${result.errorMessage}", result.exception)
                    val apiErrorMessage = result.errorMessage
                    // Ensure R.string.registration_failed_server exists
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrationError = if (apiErrorMessage != null && apiErrorMessage.isNotBlank()) null else R.string.registration_failed_server,
                            registrationErrorMessage = if (apiErrorMessage != null && apiErrorMessage.isNotBlank()) apiErrorMessage else "Registration failed. Please try again."
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(registrationError = null, registrationErrorMessage = null) }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(registrationSuccess = false) }
    }
}
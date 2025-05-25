// MODIFIED: app/src/main/java/com/example/merchantapp/viewmodel/RegisterViewModel.kt
package com.example.merchantapp.viewmodel

import android.app.Application // ADDED: For AndroidViewModel
import android.util.Log
import androidx.lifecycle.AndroidViewModel // MODIFIED: Changed from ViewModel to AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.R // Make sure this is your app's R file
import com.example.merchantapp.data.repository.MerchantRepository // Ensure this path is correct
import com.example.merchantapp.data.repository.Result // Ensure this path is correct, or if Result is defined elsewhere
import com.example.merchantapp.model.MerchantRegistrationRequest // Ensure this path is correct (com.example.merchantapp.model)
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Enum RegisterFormField remains the same
enum class RegisterFormField {
    STORE_NAME,
    CONTACT_NAME,
    EMAIL,
    PHONE,
    ADDRESS,
    PASSWORD,
    CONFIRM_PASSWORD
}

// Data class RegisterUiState remains the same
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

// MODIFIED: Class declaration to extend AndroidViewModel and take Application in constructor
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // MODIFIED: Instantiate MerchantRepository with the application context
    private val merchantRepository: MerchantRepository = MerchantRepository(application.applicationContext)

    private val minPasswordLength = 8 // Matched to backend validation

    // All on<Field>Change methods (onStoreNameChange, onContactNameChange, etc.) remain the same
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


    fun onSubmitClick() {
        // NOTE: This method does not directly use context for AuthManager,
        // so it doesn't need to explicitly get `application.applicationContext`
        // unless you add functionality here that needs it (e.g., showing a Toast, which should ideally be a side-effect triggered from UI).

        Log.d("RegisterViewModel", "Submit Clicked - Starting Validation")
        _uiState.update { it.copy(registrationError = null, registrationErrorMessage = null, showGeneralValidationError = false, invalidFields = emptySet()) }

        val currentState = _uiState.value // Capture current state once
        val validationErrors = mutableSetOf<RegisterFormField>()

        if (currentState.storeName.isBlank()) validationErrors.add(RegisterFormField.STORE_NAME)
        if (currentState.contactName.isBlank()) validationErrors.add(RegisterFormField.CONTACT_NAME)
        if (currentState.email.isBlank()) validationErrors.add(RegisterFormField.EMAIL)
        if (currentState.phone.isBlank()) validationErrors.add(RegisterFormField.PHONE)
        if (currentState.address.isBlank()) validationErrors.add(RegisterFormField.ADDRESS)
        if (currentState.password.isBlank()) validationErrors.add(RegisterFormField.PASSWORD)
        if (currentState.confirmPassword.isBlank()) validationErrors.add(RegisterFormField.CONFIRM_PASSWORD)

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
            Log.d("RegisterViewModel", "Validation Failed: Password too short (min $minPasswordLength)")
            // Ensure R.string.validation_password_too_short exists
            _uiState.update { it.copy(registrationError = R.string.validation_password_too_short, invalidFields = it.invalidFields + RegisterFormField.PASSWORD) }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            Log.d("RegisterViewModel", "Validation Failed: Passwords do not match")
            // Ensure R.string.validation_passwords_mismatch exists
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
                storeName = currentState.storeName,
                email = currentState.email,
                password = currentState.password,
                location = currentState.address,
                contactPerson = currentState.contactName,
                contactPhoneNumber = currentState.phone
            )

            Log.d("RegisterViewModel", "Registration Request being sent: $registrationRequest")

            // The merchantRepository instance is now the class member, initialized with context
            when (val result = merchantRepository.registerMerchant(registrationRequest)) {
                is Result.Success -> {
                    Log.i("RegisterViewModel", "Registration API call successful: ${result.data.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrationSuccess = true,
                            registrationError = null, // Clear previous errors on success
                            registrationErrorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    Log.e("RegisterViewModel", "Registration API call failed: ${result.errorMessage}", result.exception)
                    // Ensure R.string.registration_failed_server exists if using it as a fallback
                    val errorMessageToShow = result.errorMessage.ifBlank { "Registration failed. Please try again." }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrationError = null, // Using string message primarily
                            registrationErrorMessage = errorMessageToShow
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
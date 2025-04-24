package com.example.merchantapp.viewmodel // Make sure this matches your package structure

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // For launching coroutines
import com.example.merchantapp.data.LoginRequest // Import request data class
import com.example.merchantapp.data.LoginResponse // Import response data class
// import com.example.merchantapp.network.RetrofitInstance // No longer needed for mocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // Helper function for updating StateFlow
import kotlinx.coroutines.launch // For launching background tasks
// import retrofit2.HttpException // No longer needed for mocking
// import java.io.IOException // No longer needed for mocking
import kotlinx.coroutines.delay // ADDED: For simulating network delay

/**
 * Data class to hold the current state of the Login Screen UI.
 */
data class LoginUiState(
    val email: String = "store1@example.com", // Pre-filled like HTML
    val password: String = "password",       // Pre-filled like HTML
    val isLoading: Boolean = false,        // To show a loading indicator
    val errorMessage: String? = null,      // To display error messages
    val loginSuccess: Boolean = false      // Flag to indicate successful login
)

/**
 * ViewModel for the Login Screen.
 * Handles UI state and business logic for login.
 * --- NOW USING MOCKED NETWORK RESPONSE ---
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = newEmail,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = newPassword,
                errorMessage = null
            )
        }
    }

    fun login() {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val request = LoginRequest(email = _uiState.value.email, password = _uiState.value.password)
                Log.d("LoginViewModel", "[MOCK] Attempting login for: ${request.email}")

                // --- MODIFIED: Simulate Network Delay & Response ---
                delay(1000) // Simulate network latency (1 second)

                // Define mock successful credentials
                val mockSuccessEmail = "store1@example.com"
                val mockSuccessPassword = "password"

                // Simulate success/failure based on hardcoded values
                if (request.email == mockSuccessEmail && request.password == mockSuccessPassword) {
                    // Simulate a successful response object
                    val mockResponse = LoginResponse(
                        token = "mock_token_${System.currentTimeMillis()}", // Generate a fake token
                        merchantId = "merchant_123",
                        error = null
                    )
                    Log.d("LoginViewModel", "[MOCK] Login Success. Token: ${mockResponse.token}")
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true, errorMessage = null) }
                } else {
                    // Simulate a failure response object
                    val mockResponse = LoginResponse(
                        token = null,
                        merchantId = null,
                        error = "Invalid credentials (mocked)"
                    )
                    Log.e("LoginViewModel", "[MOCK] Login Failed: ${mockResponse.error}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = mockResponse.error) }
                }
                // --- End Simulation ---

            } catch (e: Exception) {
                // Catch potential errors in the simulation logic itself (less likely)
                Log.e("LoginViewModel", "[MOCK] Unexpected error during mock login", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Mock simulation error: ${e.message}") }
            }
        }
    }
}
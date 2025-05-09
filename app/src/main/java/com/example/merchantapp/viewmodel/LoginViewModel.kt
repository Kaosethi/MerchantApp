// LoginViewModel.kt
package com.example.merchantapp.viewmodel

import android.content.Context // ADDED: For AuthManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.data.LoginRequest
import com.example.merchantapp.data.LoginResponse
import com.example.merchantapp.data.local.AuthManager // ADDED: Import AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class LoginUiState(
    val email: String = "store1@example.com",
    val password: String = "password",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

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

    // MODIFIED: login function now accepts Context
    fun login(context: Context) {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val currentEmail = _uiState.value.email // Store for use after delay
                val currentPassword = _uiState.value.password // Store for use after delay
                val request = LoginRequest(email = currentEmail, password = currentPassword)
                Log.d("LoginViewModel", "[MOCK] Attempting login for: ${request.email}")

                delay(1000)

                val mockSuccessEmail = "store1@example.com"
                val mockSuccessPassword = "password"

                if (request.email == mockSuccessEmail && request.password == mockSuccessPassword) {
                    val mockResponse = LoginResponse(
                        token = "mock_token_${System.currentTimeMillis()}",
                        merchantId = "merchant_123",
                        error = null
                    )
                    Log.d("LoginViewModel", "[MOCK] Login Success. Token: ${mockResponse.token}")

                    // ADDED: Set logged-in state using AuthManager
                    AuthManager.setLoggedIn(context.applicationContext, true, request.email)
                    Log.d("LoginViewModel", "Saved login state to AuthManager for ${request.email}")

                    _uiState.update { it.copy(isLoading = false, loginSuccess = true, errorMessage = null) }
                } else {
                    val mockResponse = LoginResponse(
                        token = null,
                        merchantId = null,
                        error = "Invalid credentials (mocked)"
                    )
                    Log.e("LoginViewModel", "[MOCK] Login Failed: ${mockResponse.error}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = mockResponse.error) }
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "[MOCK] Unexpected error during mock login", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Mock simulation error: ${e.message}") }
            }
        }
    }

    // ADDED: Function to call when login success navigation has occurred
    // This prevents re-navigation if the user comes back to the login screen
    // while the loginSuccess flag is still true.
    fun onLoginSuccessNavigationConsumed() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
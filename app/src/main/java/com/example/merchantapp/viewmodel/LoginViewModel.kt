// MODIFIED: app/src/main/java/com/example/merchantapp/viewmodel/LoginViewModel.kt
package com.example.merchantapp.viewmodel

import android.app.Application // ADDED
// REMOVED: import android.content.Context (if it was only for the login method param)
import android.util.Log
import androidx.lifecycle.AndroidViewModel // MODIFIED: Extend AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.data.local.AuthManager
import com.example.merchantapp.data.repository.MerchantRepository
import com.example.merchantapp.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// LoginUiState data class remains the same
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) { // MODIFIED: Constructor

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // MODIFIED: Instantiate repository with application context
    private val merchantRepository: MerchantRepository = MerchantRepository(application.applicationContext)

    fun onEmailChange(newEmail: String) {
        _uiState.update { currentState ->
            currentState.copy(email = newEmail, errorMessage = null)
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(password = newPassword, errorMessage = null)
        }
    }

    // MODIFIED: login function now uses getApplication() for context
    fun login() {
        val context = getApplication<Application>().applicationContext // Use context from AndroidViewModel

        val currentEmail = _uiState.value.email
        val currentPassword = _uiState.value.password

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val request = LoginRequest(email = currentEmail, password = currentPassword)
            Log.d("LoginViewModel", "Attempting login for: ${request.email}")

            when (val result = merchantRepository.loginMerchant(request)) {
                is Result.Success -> {
                    val loginResponse = result.data
                    if (loginResponse.token != null && loginResponse.merchant != null) {
                        Log.i("LoginViewModel", "Login Success. Token: ${loginResponse.token}, Merchant: ${loginResponse.merchant.businessName}")
                        AuthManager.setLoggedIn(
                            context, // Use the fetched context
                            true,
                            loginResponse.merchant.contactEmail,
                            loginResponse.token
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loginSuccess = true,
                                errorMessage = null
                            )
                        }
                    } else {
                        Log.e("LoginViewModel", "Login successful but token or merchant data missing.")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = loginResponse.error ?: "Login failed: Incomplete server response."
                            )
                        }
                    }
                }
                is Result.Error -> {
                    Log.e("LoginViewModel", "Login Failed: ${result.errorMessage}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.errorMessage
                        )
                    }
                }
            }
        }
    }

    fun onLoginSuccessNavigationConsumed() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
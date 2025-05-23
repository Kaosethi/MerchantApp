// SetNewPasswordScreen.kt
package com.example.merchantapp.ui.setnewpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
// Removed: import androidx.lifecycle.SavedStateHandle // No longer needed directly in Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.SetNewPasswordViewModel

@Composable
fun SetNewPasswordScreen(
    viewModel: SetNewPasswordViewModel = viewModel(), // ViewModel will get args from SavedStateHandle
    onPasswordSetNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isPasswordSetSuccessfully) {
        if (uiState.isPasswordSetSuccessfully) {
            onPasswordSetNavigateToLogin()
            viewModel.onPasswordSetSuccessNavigationConsumed()
        }
    }

    SetNewPasswordScreenContent(
        uiState = uiState,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSetNewPasswordClick = { viewModel.setNewPassword() } // Ensure lambda for onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetNewPasswordScreenContent(
    uiState: SetNewPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSetNewPasswordClick: () -> Unit
) {
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Set New Password") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // MODIFIED: Use uiState.emailForContext for display
            if (!uiState.emailForContext.isNullOrBlank()) {
                Text(
                    text = "Please set a new password for your account associated with: ${uiState.emailForContext}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Optional: Show a generic message if email isn't available for display
                Text(
                    text = "Please set a new password for your account.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            OutlinedTextField(
                value = uiState.newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (newPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    val description = if (newPasswordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                isError = uiState.errorMessage != null && (uiState.errorMessage.contains("Password", ignoreCase = true) || !uiState.passwordsMatch), // Added ignoreCase
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirm New Password") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    val description = if (confirmPasswordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                isError = !uiState.passwordsMatch && uiState.confirmPassword.isNotEmpty(), // Show error only if confirm has text and mismatch
                modifier = Modifier.fillMaxWidth()
            )

            val currentPasswordRequirementsMessage = uiState.passwordRequirementsMessage
            // Removed null check if passwordRequirementsMessage is non-nullable in UiState, otherwise keep it
            Text(
                text = currentPasswordRequirementsMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            if (!uiState.passwordsMatch && uiState.confirmPassword.isNotEmpty() && uiState.newPassword.isNotEmpty()) { // Show only if both fields have text
                Text(
                    text = "Passwords do not match.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val currentErrorMessage = uiState.errorMessage
            if (currentErrorMessage != null) {
                Text(
                    text = currentErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val currentApiMessage = uiState.apiMessage // For success message from API
            if (currentApiMessage != null && !uiState.isPasswordSetSuccessfully) { // Show API message if not navigating yet
                Text(
                    text = currentApiMessage,
                    color = MaterialTheme.colorScheme.primary, // Or appropriate color
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onSetNewPasswordClick,
                // MODIFIED: Use uiState.canSubmit from ViewModel for button enabled state
                enabled = !uiState.isLoading && uiState.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Set New Password")
                }
            }
        }
    }
}

// --- PREVIEWS UPDATED ---
@Preview(showBackground = true, name = "Default State")
@Composable
fun SetNewPasswordScreenPreview() {
    MerchantAppTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(emailForContext = "preview@example.com"), // MODIFIED
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSetNewPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Passwords Mismatch")
@Composable
fun SetNewPasswordScreenErrorMismatchPreview() {
    MerchantAppTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(
                emailForContext = "user@example.com", // MODIFIED
                newPassword = "password123",
                confirmPassword = "password124",
                passwordsMatch = false,
                canSubmit = false, // Mismatch means cannot submit
                passwordRequirementsMessage = "Password must be at least 8 characters long."
            ),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSetNewPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - General Error")
@Composable
fun SetNewPasswordScreenErrorGeneralPreview() {
    MerchantAppTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(
                emailForContext = "user@example.com", // MODIFIED
                newPassword = "password123",
                confirmPassword = "password123",
                errorMessage = "A general error occurred.",
                passwordsMatch = true,
                canSubmit = true, // Assume valid passwords for this general error preview
                passwordRequirementsMessage = "Password must be at least 8 characters long."
            ),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSetNewPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun SetNewPasswordScreenLoadingPreview() {
    MerchantAppTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(
                emailForContext = "user@example.com", // MODIFIED
                newPassword = "password123",
                confirmPassword = "password123",
                isLoading = true,
                passwordsMatch = true,
                canSubmit = false // Cannot submit while loading
            ),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSetNewPasswordClick = {}
        )
    }
}
// SetNewPasswordScreen.kt
package com.example.merchantapp.ui.setnewpassword // MODIFIED: Removed Russian comment

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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.SetNewPasswordViewModel

@Composable
fun SetNewPasswordScreen(
    viewModel: SetNewPasswordViewModel = viewModel(),
    onPasswordSetNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isPasswordSetSuccessfully) {
        if (uiState.isPasswordSetSuccessfully) {
            onPasswordSetNavigateToLogin()
            viewModel.onPasswordSetSuccessNavigationConsumed() // Reset the flag
        }
    }

    SetNewPasswordScreenContent(
        uiState = uiState,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSetNewPasswordClick = viewModel::setNewPassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetNewPasswordScreenContent( // ADDED: New stateless composable for UI
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

            Text(
                text = "Please set a new password for your account associated with: ${uiState.emailOrToken}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.newPassword,
                onValueChange = onNewPasswordChange, // MODIFIED: Use passed lambda
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
                isError = uiState.errorMessage != null && (uiState.errorMessage.contains("Password") || !uiState.passwordsMatch),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange, // MODIFIED: Use passed lambda
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
                isError = !uiState.passwordsMatch,
                modifier = Modifier.fillMaxWidth()
            )

            // MODIFIED: Smart cast fix
            val currentPasswordRequirementsMessage = uiState.passwordRequirementsMessage
            if (currentPasswordRequirementsMessage != null) {
                Text(
                    text = currentPasswordRequirementsMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!uiState.passwordsMatch && uiState.confirmPassword.isNotEmpty()) {
                Text(
                    text = "Passwords do not match.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // MODIFIED: Smart cast fix
            val currentErrorMessage = uiState.errorMessage
            if (currentErrorMessage != null) {
                Text(
                    text = currentErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onSetNewPasswordClick, // MODIFIED: Use passed lambda
                enabled = !uiState.isLoading && uiState.newPassword.isNotEmpty() && uiState.confirmPassword.isNotEmpty(),
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

@Preview(showBackground = true, name = "Default State")
@Composable
fun SetNewPasswordScreenPreview() {
    MerchantAppTheme {
        SetNewPasswordScreenContent( // MODIFIED: Preview now calls the stateless content composable
            uiState = SetNewPasswordUiState(emailOrToken = "preview@example.com"),
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
        SetNewPasswordScreenContent( // MODIFIED: Preview now calls the stateless content composable
            uiState = SetNewPasswordUiState(
                emailOrToken = "user@example.com",
                newPassword = "password123",
                confirmPassword = "password124", // Mismatch
                errorMessage = "Passwords do not match.", // This specific message is handled by a separate Text now
                passwordsMatch = false, // This will trigger the "Passwords do not match" Text
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
        SetNewPasswordScreenContent( // MODIFIED: Preview now calls the stateless content composable
            uiState = SetNewPasswordUiState(
                emailOrToken = "user@example.com",
                newPassword = "password123",
                confirmPassword = "password123",
                errorMessage = "A general error occurred.", // This will be shown
                passwordsMatch = true,
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
        SetNewPasswordScreenContent( // MODIFIED: Preview now calls the stateless content composable
            uiState = SetNewPasswordUiState(
                emailOrToken = "user@example.com",
                newPassword = "password123",
                confirmPassword = "password123",
                isLoading = true,
                passwordsMatch = true
            ),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSetNewPasswordClick = {}
        )
    }
}
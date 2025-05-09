// File: app/src/main/java/com/example/merchantapp/ui/forgotpassword/ForgotPasswordScreen.kt
package com.example.merchantapp.ui.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onOtpSentNavigateToOtpEntry: (email: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess, uiState.email) {
        if (uiState.isSuccess && uiState.email.isNotBlank()) {
            onOtpSentNavigateToOtpEntry(uiState.email)
            viewModel.resetSuccessState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        ForgotPasswordContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onSubmitClick = viewModel::submitRequest
        )
    }
}

@Composable
fun ForgotPasswordContent(
    modifier: Modifier = Modifier,
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Enter your account email. We'll send a One-Time Password (OTP) to verify your email.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmitClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.email.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send OTP")
            }
        }
    }
}

// Optionally include a preview function if needed for Compose preview
@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    MerchantAppTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(email = "", isLoading = false, isSuccess = false, errorMessage = null),
            onEmailChange = {},
            onSubmitClick = {}
        )
    }
}

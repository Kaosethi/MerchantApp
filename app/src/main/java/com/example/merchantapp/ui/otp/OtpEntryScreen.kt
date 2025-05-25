// File: app/src/main/java/com/example/merchantapp/ui/otp/OtpEntryScreen.kt
package com.example.merchantapp.ui.otp

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel // Standard way to get ViewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.OtpEntryViewModel

// OTP_LENGTH is defined in OtpEntryViewModel, but can be defined here too if UI needs it directly
// private const val OTP_LENGTH = 6 // Or use com.example.merchantapp.viewmodel.OTP_LENGTH

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpEntryScreen(
    // The ViewModel will get the 'email' argument from SavedStateHandle via NavHost
    viewModel: OtpEntryViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    // MODIFIED: Lambda now takes both email and the reset token
    onOtpVerifiedNavigateToSetPassword: (email: String, resetToken: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Effect to handle navigation when OTP is verified and token is available
    LaunchedEffect(uiState.isOtpVerified, uiState.resetAuthToken) {
        if (uiState.isOtpVerified && uiState.resetAuthToken != null) {
            Log.d("OtpEntryScreen", "OTP Verified. Navigating to SetNewPassword. Email: ${uiState.email}, Token: ${uiState.resetAuthToken}")
            // Call the navigation lambda with both email and the non-null reset token
            onOtpVerifiedNavigateToSetPassword(uiState.email, uiState.resetAuthToken!!) // Pass both
            viewModel.resetOtpVerifiedStateAndToken() // Reset ViewModel state after initiating navigation
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter OTP") },
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
        OtpEntryContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onOtpChange = viewModel::onOtpChange, // Pass method reference
            onVerifyOtpClick = { viewModel.verifyOtp() }, // Use lambda to call
            onResendOtpClick = { viewModel.resendOtp() }  // Use lambda to call
        )
    }
}

@Composable
fun OtpEntryContent(
    modifier: Modifier = Modifier,
    uiState: OtpEntryUiState,
    onOtpChange: (String) -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter the 6-digit OTP sent to:",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = uiState.email, // Display the email received by the ViewModel
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = uiState.otpValue,
            onValueChange = onOtpChange,
            label = { Text("OTP") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            // Show error styling if errorMessage is present and it's not just an info message about new OTP
            isError = uiState.errorMessage != null && uiState.apiMessage == null, // Simpler error condition
            enabled = !uiState.isLoading
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 24.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val messageToDisplay = uiState.apiMessage ?: uiState.errorMessage
            if (messageToDisplay != null) {
                Text(
                    text = messageToDisplay,
                    color = if (uiState.errorMessage != null && uiState.apiMessage == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyOtpClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.otpValue.length == com.example.merchantapp.viewmodel.OTP_LENGTH // Use constant from VM
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Verify OTP")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Didn't receive OTP? ")
            TextButton(
                onClick = onResendOtpClick,
                enabled = uiState.isResendEnabled && !uiState.isLoading
            ) {
                Text(
                    if (uiState.isResendEnabled) "Resend OTP"
                    else "Resend OTP in ${uiState.resendTimerSeconds}s"
                )
            }
        }
    }
}


// Preview Functions (remain largely the same, ensure OtpEntryUiState is up-to-date if it changed)
@Preview(showBackground = true, name = "Default")
@Composable
fun OtpEntryScreenPreview_Default() {
    MerchantAppTheme {
        Scaffold { padding ->
            OtpEntryContent(
                modifier = Modifier.padding(padding),
                uiState = OtpEntryUiState(email = "preview@example.com"),
                onOtpChange = {},
                onVerifyOtpClick = {},
                onResendOtpClick = {}
            )
        }
    }
}

// Add other previews as before (Loading, Error, ResendCooldown, ResendEnabled)
// Ensure the OtpEntryUiState used in previews matches its current definition.
// For example, if apiMessage or resetAuthToken were added, include them if relevant for preview.

@Preview(showBackground = true, name = "Loading")
@Composable
fun OtpEntryScreenPreview_Loading() {
    MerchantAppTheme {
        Scaffold { padding ->
            OtpEntryContent(
                modifier = Modifier.padding(padding),
                uiState = OtpEntryUiState(email = "preview@example.com", otpValue = "123456", isLoading = true),
                onOtpChange = {},
                onVerifyOtpClick = {},
                onResendOtpClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun OtpEntryScreenPreview_Error() {
    MerchantAppTheme {
        Scaffold { padding ->
            OtpEntryContent(
                modifier = Modifier.padding(padding),
                uiState = OtpEntryUiState(email = "preview@example.com", otpValue = "111110", errorMessage = "Invalid OTP."),
                onOtpChange = {},
                onVerifyOtpClick = {},
                onResendOtpClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Resend Cooldown")
@Composable
fun OtpEntryScreenPreview_ResendCooldown() {
    MerchantAppTheme {
        Scaffold { padding ->
            OtpEntryContent(
                modifier = Modifier.padding(padding),
                uiState = OtpEntryUiState(email = "preview@example.com", resendTimerSeconds = 35, isResendEnabled = false),
                onOtpChange = {},
                onVerifyOtpClick = {},
                onResendOtpClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Resend Enabled")
@Composable
fun OtpEntryScreenPreview_ResendEnabled() {
    MerchantAppTheme {
        Scaffold { padding ->
            OtpEntryContent(
                modifier = Modifier.padding(padding),
                uiState = OtpEntryUiState(email = "preview@example.com", resendTimerSeconds = 0, isResendEnabled = true),
                onOtpChange = {},
                onVerifyOtpClick = {},
                onResendOtpClick = {}
            )
        }
    }
}
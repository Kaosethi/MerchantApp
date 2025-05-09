// File: app/src/main/java/com/example/merchantapp/ui/otp/OtpEntryScreen.kt
package com.example.merchantapp.ui.otp

// Import the UiState from its correct location
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.OtpEntryViewModel

private const val OTP_LENGTH = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpEntryScreen(
    // email: String, // ViewModel will get this from SavedStateHandle
    viewModel: OtpEntryViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onOtpVerifiedNavigateToSetPassword: (emailOrToken: String) -> Unit, // Pass identifier to next screen
    onRequestResendOtp: () -> Unit // Could be handled by ViewModel directly too
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isOtpVerified) {
        if (uiState.isOtpVerified) {
            // OTP is verified, navigate to Set New Password screen
            // We pass the email as the identifier for now
            onOtpVerifiedNavigateToSetPassword(uiState.email)
            viewModel.resetOtpVerifiedState() // Reset state after navigation
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
            onOtpChange = viewModel::onOtpChange,
            onVerifyOtpClick = viewModel::verifyOtp,
            onResendOtpClick = viewModel::resendOtp // Call VM's resendOtp
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
            text = uiState.email, // Display the email
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = uiState.otpValue,
            onValueChange = onOtpChange,
            label = { Text("OTP") },
            modifier = Modifier.fillMaxWidth(0.8f), // Make it a bit narrower
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword // Use NumberPassword for OTP
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center), // Center text
            isError = uiState.errorMessage != null && !uiState.errorMessage.contains("New OTP 'sent'"), // Don't show error styling for "New OTP sent" info
            enabled = !uiState.isLoading
        )

        // Display error message or info message
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 24.dp) // Ensure space for message
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = if (uiState.errorMessage.contains("New OTP 'sent'")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyOtpClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.otpValue.length == OTP_LENGTH
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

// ============================================================
// Preview Functions
// ============================================================

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
// File: app/src/main/java/com/example/merchantapp/ui/login/LoginScreen.kt
package com.example.merchantapp.ui.login

// Essential Imports (Ensure all needed imports are present)
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.LoginUiState
import com.example.merchantapp.viewmodel.LoginViewModel

// ============================================================
// Stateful Composable - Connects to ViewModel
// ============================================================
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            Log.d("LoginScreen", "Login successful, navigating...")
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onRegisterClick = onNavigateToRegister,
        onForgotPasswordClick = onNavigateToForgotPassword
    )
}

// ============================================================
// Stateless Composable - Displays the UI based on state
// ============================================================
@OptIn(ExperimentalMaterial3Api::class) // ADDED: Opt-in for Card usage
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // --- App Icon and Title ---
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = "App Logo", // Parameter should exist
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary // Parameter should exist
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Merchant Portal", // Parameter should exist
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        // --- Login Form in a Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email Input Field
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange, // Should be used
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                ) // Call should be valid
                Spacer(modifier = Modifier.height(16.dp))

                // Password Input Field
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange, // Should be used
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !uiState.isLoading
                ) // Call should be valid
                Spacer(modifier = Modifier.height(8.dp))

                // Error Message Display Area
                if (uiState.errorMessage != null) {
                    Text( // This block should not be empty
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                } else {
                    // Placeholder space when no error
                    Spacer(modifier = Modifier.height(22.dp)) // This block should not be empty
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Sign In Button
                Button(
                    onClick = onLoginClick, // Should be used, parameter should exist
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign In")
                    }
                } // Call should be valid
                Spacer(modifier = Modifier.height(16.dp))

                // Register Link
                TextButton(
                    onClick = onRegisterClick,
                    enabled = !uiState.isLoading
                ) {
                    Text("Register New Store")
                }

                // Forgot Password TextButton
                TextButton(
                    onClick = onForgotPasswordClick, // Should be used
                    enabled = !uiState.isLoading
                ) {
                    Text("Forgot Password?")
                } // Call should be valid
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- Footer Text ---
        Text(
            text = "Powered by IPPS", // Parameter should exist
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// ============================================================
// Preview Functions (Add OptIn for Card)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class) // ADDED OptIn
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LoginScreenPreview_DefaultState() {
    MerchantAppTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class) // ADDED OptIn
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LoginScreenPreview_Loading() {
    MerchantAppTheme {
        LoginScreenContent(
            uiState = LoginUiState(isLoading = true, email = "test@test.com", password = "password"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class) // ADDED OptIn
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LoginScreenPreview_Error() {
    MerchantAppTheme {
        LoginScreenContent(
            uiState = LoginUiState(errorMessage = "Invalid credentials", email = "test@test.com", password = "wrong"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}
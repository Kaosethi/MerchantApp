package com.example.merchantapp.ui.login // Make sure this matches your package structure

// Essential Imports for Layout and UI Elements
import android.util.Log
import android.widget.Toast // ADDED: For showing simple messages
import androidx.compose.foundation.layout.* // Keep wildcard imports for layout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.* // Keep wildcard import for Material 3 components
import androidx.compose.runtime.* // Keep wildcard import for Compose runtime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // ADDED: To show Toast messages
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ViewModel and StateFlow related imports
import androidx.lifecycle.viewmodel.compose.viewModel // ADDED: For getting ViewModel instance
import androidx.lifecycle.compose.collectAsStateWithLifecycle // ADDED: For collecting StateFlow safely
import com.example.merchantapp.viewmodel.LoginUiState // ADDED: Import the UI State class
import com.example.merchantapp.viewmodel.LoginViewModel // ADDED: Import the ViewModel class

// Import your AppTheme
import com.example.merchantapp.ui.theme.MerchantAppTheme

// ============================================================
// Stateful Composable - Connects to ViewModel
// ============================================================
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(), // Get instance provided by lifecycle library
    onNavigateToRegister: () -> Unit, // Callback for navigating to registration
    onLoginSuccess: () -> Unit // Callback for navigating after successful login
) {
    // Collect the UI state from the ViewModel in a lifecycle-aware manner
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current // Get context for showing Toasts

    // Use LaunchedEffect to react to loginSuccess state change
    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            Log.d("LoginScreen", "Login successful, navigating...")
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            // TODO: Perform navigation or other actions on successful login
            onLoginSuccess()
        }
    }

    // Call the stateless content composable, passing state and event handlers
    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange, // Pass ViewModel functions as callbacks
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onRegisterClick = onNavigateToRegister // Pass navigation callback through
    )
}

// ============================================================
// Stateless Composable - Displays the UI based on state
// ============================================================
@Composable
fun LoginScreenContent(
    uiState: LoginUiState, // Receive state as parameter
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // UI Layout: Arranging elements vertically and centered
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
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Merchant Portal",
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
                modifier = Modifier.padding(24.dp)
            ) {
                // Email Input Field - Value and action driven by uiState and callbacks
                OutlinedTextField(
                    value = uiState.email, // MODIFIED: Use value from uiState
                    onValueChange = onEmailChange, // MODIFIED: Call provided callback
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(), // Make field wide
                    singleLine = true,
                    enabled = !uiState.isLoading // MODIFIED: Disable when loading
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password Input Field - Value and action driven by uiState and callbacks
                OutlinedTextField(
                    value = uiState.password, // MODIFIED: Use value from uiState
                    onValueChange = onPasswordChange, // MODIFIED: Call provided callback
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !uiState.isLoading // MODIFIED: Disable when loading
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Error Message Display Area - Driven by uiState
                if (uiState.errorMessage != null) { // MODIFIED: Check error in uiState
                    Text(
                        text = uiState.errorMessage, // MODIFIED: Show error from uiState
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                } else {
                    // Placeholder space when no error
                    Spacer(modifier = Modifier.height(22.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Sign In Button - Action driven by callback, state driven by uiState
                Button(
                    onClick = onLoginClick, // MODIFIED: Call provided callback
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading // MODIFIED: Disable when loading
                ) {
                    // MODIFIED: Show loading indicator or text
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign In")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Register Link
                TextButton(
                    onClick = onRegisterClick, // Use provided callback
                    enabled = !uiState.isLoading // MODIFIED: Disable when loading
                ) {
                    Text("Register New Store")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- Footer Text ---
        Text(
            text = "Powered by IPPS",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// ============================================================
// Preview Function - Previews the stateless content
// ============================================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LoginScreenPreview_DefaultState() {
    MerchantAppTheme {
        // Preview the stateless content with default state
        LoginScreenContent(
            uiState = LoginUiState(), // Use default state
            onEmailChange = {}, // Dummy actions for preview
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LoginScreenPreview_Loading() {
    MerchantAppTheme {
        // Preview the stateless content in loading state
        LoginScreenContent(
            uiState = LoginUiState(isLoading = true, email = "test@test.com", password = "password"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LoginScreenPreview_Error() {
    MerchantAppTheme {
        // Preview the stateless content with an error message
        LoginScreenContent(
            uiState = LoginUiState(errorMessage = "Invalid credentials", email = "test@test.com", password = "wrong"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}
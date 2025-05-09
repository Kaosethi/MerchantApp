// File: app/src/main/java/com/example/merchantapp/ui/login/LoginScreen.kt
package com.example.merchantapp.ui.login

// --- Imports --- //
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.R
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.LoginUiState
import com.example.merchantapp.viewmodel.LoginViewModel

// --- End Imports --- //

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
            viewModel.onLoginSuccessNavigationConsumed()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = { viewModel.login(context.applicationContext) },
        onRegisterClick = onNavigateToRegister,
        onForgotPasswordClick = onNavigateToForgotPassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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

        // --- MODIFIED: Replaced Icon with Image ---
        Image(
            painter = painterResource(id = R.drawable.payforu_logo), // Make sure 'payforu_logo.png' exists in res/drawable
            contentDescription = "Payforu Logo",
            modifier = Modifier
                .height(60.dp) // Adjust height as needed, width will scale proportionally with Fit
                .fillMaxWidth(0.6f), // Control width relative to screen (adjust fraction 0.0f - 1.0f)
            contentScale = ContentScale.Fit // Scales the image to fit within the bounds, maintaining aspect ratio
        )
        // --- End Modification ---

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Merchant Portal",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

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
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(22.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLoginClick,
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
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onRegisterClick,
                    enabled = !uiState.isLoading
                ) {
                    Text("Register New Store")
                }

                TextButton(
                    onClick = onForgotPasswordClick,
                    enabled = !uiState.isLoading
                ) {
                    Text("Forgot Password?")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Powered by IPPS", // You might want to change this too?
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// --- Preview Functions ---
// Note: Previews will now show the Image instead of the Icon.
// Ensure 'payforu_logo.png' is added correctly for previews to work.
@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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
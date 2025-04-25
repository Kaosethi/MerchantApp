// File: app/src/main/java/com/example/merchantapp/ui/register/RegisterScreen.kt
package com.example.merchantapp.ui.register

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// import androidx.compose.material.icons.filled.Language // REMOVED
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.R
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.viewmodel.RegisterViewModel
import com.example.merchantapp.viewmodel.RegisterUiState
import com.example.merchantapp.viewmodel.RegisterFormField

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(),
    onNavigateBack: () -> Unit
    // REMOVED: currentLanguage parameter
    // REMOVED: onChangeLanguage parameter
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // REMOVED: State for language menu visibility

    // --- Effects for Toast/Success (Keep these) ---
    LaunchedEffect(key1 = uiState.registrationError) {
        uiState.registrationError?.let { errorResId ->
            val message = context.getString(errorResId)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    LaunchedEffect(key1 = uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            Toast.makeText(context, R.string.registration_successful, Toast.LENGTH_LONG).show()
            viewModel.consumeSuccess()
            onNavigateBack()
        }
    }
    // --- End Effects ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_title)) },
                navigationIcon = {
                    IconButton(onClick = { if (!uiState.isLoading) onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    // REMOVED: Language selector Box, IconButton, DropdownMenu
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    // REMOVED: actionIconContentColor if no actions left
                )
            )
        }
    ) { paddingValues ->
        // Column and form content remain the same
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.register_instructions),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Form Fields ---
            RegistrationTextField(
                value = uiState.storeName,
                onValueChange = viewModel::onStoreNameChange,
                labelResId = R.string.label_store_name,
                placeholderResId = R.string.placeholder_store_name,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = uiState.invalidFields.contains(RegisterFormField.STORE_NAME),
                enabled = !uiState.isLoading
            )
            RegistrationTextField(
                value = uiState.contactName,
                onValueChange = viewModel::onContactNameChange,
                labelResId = R.string.label_contact_name,
                placeholderResId = R.string.placeholder_contact_name,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = uiState.invalidFields.contains(RegisterFormField.CONTACT_NAME),
                enabled = !uiState.isLoading
            )
            RegistrationTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                labelResId = R.string.label_email,
                placeholderResId = R.string.placeholder_email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.invalidFields.contains(RegisterFormField.EMAIL),
                enabled = !uiState.isLoading
            )
            RegistrationTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                labelResId = R.string.label_phone,
                placeholderResId = R.string.placeholder_phone,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.invalidFields.contains(RegisterFormField.PHONE),
                enabled = !uiState.isLoading
            )
            RegistrationTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                labelResId = R.string.label_address,
                placeholderResId = R.string.placeholder_address,
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.invalidFields.contains(RegisterFormField.ADDRESS),
                enabled = !uiState.isLoading
            )
            RegistrationTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                labelResId = R.string.label_password,
                placeholderResId = R.string.placeholder_password,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.invalidFields.contains(RegisterFormField.PASSWORD),
                supportingText = { Text(stringResource(R.string.label_password_requirement)) },
                enabled = !uiState.isLoading
            )
            RegistrationTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                labelResId = R.string.label_confirm_password,
                placeholderResId = R.string.placeholder_confirm_password,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.invalidFields.contains(RegisterFormField.CONFIRM_PASSWORD),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- General Validation Error Message ---
            if (uiState.showGeneralValidationError) {
                Text(
                    text = stringResource(R.string.validation_fill_all_fields),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- Submit Button ---
            Button(
                onClick = viewModel::onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(stringResource(R.string.button_submit_registration))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helper TextField Composable (Unchanged)
@Composable
private fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelResId: Int,
    placeholderResId: Int,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    isError: Boolean = false,
    enabled: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Row {
                Text(stringResource(labelResId))
                if (isError) {
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        stringResource(R.string.validation_required_field),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        placeholder = { Text(stringResource(placeholderResId)) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        isError = isError,
        enabled = enabled,
        supportingText = supportingText
    )
}


@Preview(showBackground = true, device = "id:pixel_5") // REMOVED locale="en"
// @Preview(showBackground = true, device = "id:pixel_5", locale = "th") // REMOVED Thai Preview
@Composable
fun RegisterScreenPreview() {
    MerchantAppTheme {
        RegisterScreen(
            onNavigateBack = {}
            // REMOVED language parameters from preview call
        )
    }
}
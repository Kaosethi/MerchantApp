// File: app/src/main/java/com/example/merchantapp/ui/register/RegisterScreen.kt
package com.example.merchantapp.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use auto-mirrored icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.merchantapp.ui.theme.MerchantAppTheme

@OptIn(ExperimentalMaterial3Api::class) // Needed for TopAppBar
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit, // Callback to go back
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merchant Registration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Use auto-mirrored
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp), // Add own padding
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Registration Screen Placeholder")
            // TODO: Implement Registration Form
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MerchantAppTheme {
        RegisterScreen(onNavigateBack = {})
    }
}
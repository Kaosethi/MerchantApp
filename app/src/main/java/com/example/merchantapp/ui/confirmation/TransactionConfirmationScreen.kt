// File: app/src/main/java/com/example/merchantapp/ui/confirmation/TransactionConfirmationScreen.kt
package com.example.merchantapp.ui.confirmation

// --- Imports ---
// Use Optimize Imports (Ctrl+Alt+O / Cmd+Option+O) after pasting
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.merchantapp.R
import com.example.merchantapp.ui.theme.MerchantAppTheme
import java.text.NumberFormat
import java.util.Locale

// REMOVED: URLDecoder, StandardCharsets imports
// --- End Imports ---

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TransactionConfirmationScreen(
    amount: String,
    beneficiaryId: String,
    // Parameter is now raw beneficiaryName
    beneficiaryName: String,
    onNavigateBack: () -> Unit
    // REMOVED: onNavigateToPinEntry parameter
) {
    // State variables require 'var'
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // REMOVED: Decoding logic

    // Categories are defined and used in the dropdown
    val categories = remember {
        listOf("Groceries", "Household", "Transport", "Utilities", "Education", "Healthcare", "Other")
    }

    // formattedAmount is defined and used in Amount Card
    val formattedAmount = remember(amount) {
        try {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale("en", "US")).format(amountValue)
        } catch (e: Exception) {
            Log.e("ConfirmScreen", "Failed to format amount: $amount", e)
            "Error" // Return fallback string
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.confirm_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Uses onNavigateBack
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(/*...*/)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --- Account Holder Card --- (Uses beneficiaryName, beneficiaryId)
            Card(
                modifier = Modifier.fillMaxWidth(), /* etc */
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { /* Icon/Title */ }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.confirm_label_name).uppercase(), /*...*/)
                    Text(beneficiaryName, /*...*/) // Uses raw parameter
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.confirm_label_account_id).uppercase(), /*...*/)
                    Text(beneficiaryId, /*...*/)
                }
            }

            // --- Amount Card --- (Uses formattedAmount)
            Card(
                modifier = Modifier.fillMaxWidth(), /* etc */
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.confirm_label_amount_to_charge), /*...*/)
                    Spacer(Modifier.height(4.dp))
                    Text(formattedAmount, /*...*/) // Uses formattedAmount
                }
            }

            // --- Category Dropdown --- (Uses categories)
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true, /* etc */
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .clickable { isDropdownExpanded = true }
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false } // Keep this fix
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Confirm Button --- (onClick does nothing for now)
            Button(
                enabled = selectedCategory.isNotEmpty(),
                onClick = {
                    Log.d("ConfirmScreen", "Confirm button clicked (No Nav). Category: $selectedCategory")
                    // TODO: Implement transaction processing logic here later
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(52.dp)
            ) {
                Text(stringResource(R.string.confirm_button_confirm), style = MaterialTheme.typography.labelLarge)
            }
        } // End Column
    } // End Scaffold
}

// --- Preview ---
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun TransactionConfirmationScreenPreview() {
    MerchantAppTheme {
        TransactionConfirmationScreen(
            amount = "5.00",
            beneficiaryId = "BEN-123",
            beneficiaryName = "Test User", // Pass raw name to preview
            onNavigateBack = {}
        )
    }
}
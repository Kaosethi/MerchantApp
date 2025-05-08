// File: app/src/main/java/com/example/merchantapp/ui/confirmation/TransactionConfirmationScreen.kt
package com.example.merchantapp.ui.confirmation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TransactionConfirmationScreen(
    amount: String,
    beneficiaryId: String,
    beneficiaryName: String,
    onNavigateBack: () -> Unit,
    onConfirmAndProcess: (amount: String, beneficiaryId: String, beneficiaryName: String, category: String) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val categories = remember {
        listOf("Groceries", "Household", "Transport", "Utilities", "Education", "Healthcare", "Other")
    }

    val formattedAmount = remember(amount) {
        try {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            // MODIFIED: Changed Locale to th_TH for Thai Baht
            NumberFormat.getCurrencyInstance(Locale("th", "TH")).format(amountValue)
        } catch (e: Exception) {
            Log.e("ConfirmScreen", "Failed to format amount: $amount", e)
            "Error"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.confirm_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
                    Text(stringResource(R.string.confirm_label_name).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(beneficiaryName, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.confirm_label_account_id).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(beneficiaryId, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)) // Consider using theme colors
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.confirm_label_amount_to_charge), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(formattedAmount, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF388E3C)) // Consider using theme colors
                }
            }

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = {
                    isDropdownExpanded = !isDropdownExpanded
                    Log.d("ConfirmScreen", "Dropdown expanded changed to: $isDropdownExpanded (new val will be ${!isDropdownExpanded})")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory.ifBlank { stringResource(R.string.confirm_select_category_placeholder) },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.confirm_label_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                isDropdownExpanded = false
                                Log.d("ConfirmScreen", "Category selected: $category")
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = selectedCategory.isNotEmpty(),
                onClick = {
                    Log.d("ConfirmScreen", "Confirm button clicked. Category: $selectedCategory, Amount: $amount, BeneficiaryID: $beneficiaryId")
                    onConfirmAndProcess(amount, beneficiaryId, beneficiaryName, selectedCategory)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.confirm_button_confirm), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun TransactionConfirmationScreenPreview() {
    MerchantAppTheme {
        TransactionConfirmationScreen(
            amount = "150.00", // Example THB amount
            beneficiaryId = "BEN-123456789",
            beneficiaryName = "Preview User Name",
            onNavigateBack = {},
            onConfirmAndProcess = { _, _, _, _ -> }
        )
    }
}
// File: app/src/main/java/com/example/merchantapp/ui/summary/TransactionSummaryScreen.kt
package com.example.merchantapp.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.merchantapp.ui.theme.MerchantAppTheme

@Composable
fun TransactionSummaryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Transaction Summary Screen")
        // TODO: Implement Filter controls and Transaction List
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionSummaryScreenPreview() {
    MerchantAppTheme {
        TransactionSummaryScreen()
    }
}
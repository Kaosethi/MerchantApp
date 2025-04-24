// File: app/src/main/java/com/example/merchantapp/ui/amount/AmountEntryScreen.kt
package com.example.merchantapp.ui.amount

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
fun AmountEntryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Amount Entry Screen (Home)")
        // TODO: Implement Amount Display, Keypad, Next Button etc.
    }
}

@Preview(showBackground = true)
@Composable
fun AmountEntryScreenPreview() {
    MerchantAppTheme {
        AmountEntryScreen()
    }
}
// File: app/src/main/java/com/example/merchantapp/ui/analytics/AnalyticsDashboardScreen.kt
package com.example.merchantapp.ui.analytics

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
fun AnalyticsDashboardScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Analytics Dashboard Screen")
        // TODO: Implement Date Filter and Stat Cards/Charts
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsDashboardScreenPreview() {
    MerchantAppTheme {
        AnalyticsDashboardScreen()
    }
}
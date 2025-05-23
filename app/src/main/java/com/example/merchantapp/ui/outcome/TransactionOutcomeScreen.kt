package com.example.merchantapp.ui.outcome // Or your preferred UI package for this screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // For potential back navigation in TopAppBar
import androidx.compose.material.icons.filled.ErrorOutline // General error, more subtle than solid Error
import androidx.compose.material.icons.filled.Lock // For locked
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied // For declined states
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.merchantapp.ui.theme.MerchantAppTheme

// This enum should be accessible by AppDestinations.kt and PinEntryViewModel.kt
// If AppDestinations.kt is in a different module or can't directly access this,
// you might need to define this enum in a common module/package or pass its .name as String.
enum class OutcomeType {
    LOCKED_PIN_ENTRY,
    DECLINED_INSUFFICIENT_FUNDS,
    DECLINED_GENERAL, // For other transaction declines (e.g., bank, fraud rule)
    ERROR_NETWORK,    // For network specific issues
    ERROR_GENERAL     // Fallback for other unexpected errors
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionOutcomeScreen(
    outcomeType: OutcomeType,
    titleText: String,
    messageText: String,
    buttonText: String,
    onAcknowledge: () -> Unit,
    showNavigateBack: Boolean = false, // Optionally show a back arrow in TopAppBar
    onNavigateBack: (() -> Unit)? = null // Callback if back arrow is shown
) {
    val icon: ImageVector
    val iconTint: Color

    when (outcomeType) {
        OutcomeType.LOCKED_PIN_ENTRY -> {
            icon = Icons.Filled.Lock
            iconTint = MaterialTheme.colorScheme.error
        }
        OutcomeType.DECLINED_INSUFFICIENT_FUNDS -> {
            icon = Icons.Filled.SentimentVeryDissatisfied // Or a specific money-related error icon
            iconTint = MaterialTheme.colorScheme.error
        }
        OutcomeType.DECLINED_GENERAL -> {
            icon = Icons.Filled.SentimentVeryDissatisfied
            iconTint = MaterialTheme.colorScheme.error
        }
        OutcomeType.ERROR_NETWORK -> {
            icon = Icons.Filled.ErrorOutline // Or a Wi-Fi off icon
            iconTint = MaterialTheme.colorScheme.error
        }
        OutcomeType.ERROR_GENERAL -> {
            icon = Icons.Filled.ErrorOutline
            iconTint = MaterialTheme.colorScheme.error
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleText, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    if (showNavigateBack && onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = onAcknowledge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp) // More vertical padding for bottom bar
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp), // Consistent button shape
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp), // Consistent horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Vertically center content
        ) {
            Spacer(modifier = Modifier.weight(0.2f)) // Push content down a bit from top

            Icon(
                imageVector = icon,
                contentDescription = null, // Title text already covers accessibility here
                modifier = Modifier.size(80.dp),
                tint = iconTint
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = titleText, // Re-iterate title as main heading in body
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = messageText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(0.8f)) // Pushes content towards center and button to bottom
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Account Locked Preview")
@Composable
fun AccountLockedPreview() {
    MerchantAppTheme {
        TransactionOutcomeScreen(
            outcomeType = OutcomeType.LOCKED_PIN_ENTRY,
            titleText = "PIN Entry Locked",
            messageText = "For security, PIN entry has been locked due to too many incorrect attempts. Please try again later or contact support.",
            buttonText = "Return to Dashboard",
            onAcknowledge = {}
        )
    }
}

@Preview(showBackground = true, name = "Insufficient Funds Preview")
@Composable
fun InsufficientFundsPreview() {
    MerchantAppTheme {
        TransactionOutcomeScreen(
            outcomeType = OutcomeType.DECLINED_INSUFFICIENT_FUNDS,
            titleText = "Transaction Declined",
            messageText = "Unfortunately, the transaction could not be processed due to insufficient funds.",
            buttonText = "OK", // Or "Try Different Amount"
            onAcknowledge = {}
        )
    }
}

@Preview(showBackground = true, name = "General Error Preview")
@Composable
fun GeneralErrorPreview() {
    MerchantAppTheme {
        TransactionOutcomeScreen(
            outcomeType = OutcomeType.ERROR_GENERAL,
            titleText = "Something Went Wrong",
            messageText = "We encountered an unexpected issue. Please try again. If the problem persists, contact support.",
            buttonText = "Dismiss",
            onAcknowledge = {}
        )
    }
}

@Preview(showBackground = true, name = "Network Error Preview")
@Composable
fun NetworkErrorPreview() {
    MerchantAppTheme {
        TransactionOutcomeScreen(
            outcomeType = OutcomeType.ERROR_NETWORK,
            titleText = "Network Connection Lost",
            messageText = "Please check your internet connection and try again.",
            buttonText = "Retry", // Example, actual retry logic would be needed
            onAcknowledge = {}
        )
    }
}
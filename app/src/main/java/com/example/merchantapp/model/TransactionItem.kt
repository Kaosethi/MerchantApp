// TransactionItem.kt
package com.example.merchantapp.model

import java.time.LocalDateTime // Using java.time for modern date/time handling

// ADDED: Entire new file

enum class TransactionStatus {
    APPROVED,
    DECLINED,
    PENDING // Added for completeness, though your example only shows Approved/Declined
}

sealed class DeclineReason(val message: String) {
    object AccountNotFound : DeclineReason("Account Not Found")
    object AccountInactive : DeclineReason("Account Inactive")
    object IncorrectPin : DeclineReason("Incorrect PIN")
    object InsufficientBalance : DeclineReason("Insufficient Balance")
    data class Other(val customMessage: String) : DeclineReason(customMessage) // For any other backend reasons
    object None : DeclineReason("") // When not declined
}

data class TransactionItem(
    val id: String, // e.g., "T006" - unique identifier for the transaction
    val fullTransactionId: String, // Potentially a longer, more globally unique ID from backend
    val dateTime: LocalDateTime,
    val accountId: String, // Payer's account ID
    val accountOwnerName: String, // Payer's name
    val amount: Double,
    val status: TransactionStatus,
    val category: String, // Type of goods sold
    val reasonForDecline: DeclineReason? = null // Null if not declined or reason not applicable
)
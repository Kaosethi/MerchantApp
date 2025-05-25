package com.example.merchantapp.model // Ensure this is the same package as TransactionApiResponse.kt

import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
// No need to import TransactionApiResponse if it's in the same package.
// If it were in a different package, you'd use: import com.example.merchantapp.model.api.TransactionApiResponse (example)

enum class TransactionStatus {
    APPROVED,
    DECLINED,
    PENDING,
    FAILED
}

sealed class DeclineReason(val message: String) {
    object InsufficientBalance : DeclineReason("Insufficient Balance")
    object AccountSuspended : DeclineReason("Account Suspended")
    object AccountNotActive : DeclineReason("Account Not Active")
    object IncorrectPin : DeclineReason("Incorrect PIN")
    object AccountNotFound : DeclineReason("Account Not Found")
    data class Other(val customMessage: String) : DeclineReason(customMessage)
    object None : DeclineReason("")
}

data class TransactionItem(
    val id: String,
    val fullTransactionId: String,
    val dateTime: LocalDateTime,
    val accountId: String,
    val accountOwnerName: String,
    val amount: Double,
    val status: TransactionStatus,
    val category: String,
    val reasonForDecline: DeclineReason? = null
)

fun TransactionApiResponse.toTransactionItem(): TransactionItem? {
    val parsedDateTime = try {
        this.dateTime?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
    } catch (e: DateTimeParseException) {
        Log.e("TransactionMapper", "Failed to parse date: ${this.dateTime} for API transaction ID: ${this.id}", e)
        null
    }

    val parsedStatus: TransactionStatus = when (this.status?.lowercase()?.trim()) {
        "completed", "approved" -> TransactionStatus.APPROVED
        "pending" -> TransactionStatus.PENDING
        "declined" -> TransactionStatus.DECLINED
        "failed" -> TransactionStatus.FAILED
        else -> {
            Log.w("TransactionMapper", "Unknown transaction status from API: '${this.status}' for API transaction ID: ${this.id}")
            TransactionStatus.FAILED // Default to FAILED or handle as a critical mapping error
        }
    }

    val parsedDeclineReason: DeclineReason = this.declineReason?.takeIf { it.isNotBlank() }?.let { reasonString ->
        val lowerReason = reasonString.lowercase().trim()
        when {
            lowerReason.contains("insufficient") && lowerReason.contains("funds") -> DeclineReason.InsufficientBalance
            lowerReason.contains("insufficient balance") -> DeclineReason.InsufficientBalance
            lowerReason.contains("suspended") -> DeclineReason.AccountSuspended
            // More specific checks for backend messages indicating these states:
            lowerReason.contains("not active") -> DeclineReason.AccountNotActive
            lowerReason.contains("inactive") -> DeclineReason.AccountNotActive // Map "inactive" to generic "NotActive"
            lowerReason.contains("pending") -> DeclineReason.AccountNotActive  // Map "pending" to generic "NotActive"
            lowerReason.contains("incorrect pin") -> DeclineReason.IncorrectPin // If backend ever sends this
            lowerReason.contains("not found") -> DeclineReason.AccountNotFound // If backend indicates "not found" in decline reason
            else -> DeclineReason.Other(reasonString)
        }
    } ?: if (parsedStatus == TransactionStatus.DECLINED || parsedStatus == TransactionStatus.FAILED) {
        DeclineReason.Other("Reason not specified by server.")
    } else {
        DeclineReason.None
    }

    // Check for essential fields from TransactionApiResponse
    // Using 'let' for safer access to nullable fields after checks
    val currentId = this.id
    val currentShortId = this.shortId
    val currentAccountId = this.accountId
    val currentAccountOwnerName = this.accountOwnerName
    val currentAmount = this.amount
    val currentCategory = this.category

    if (currentId == null ||
        currentShortId == null ||
        parsedDateTime == null || // Already checked but good for clarity
        currentAccountId == null ||
        currentAccountOwnerName == null ||
        currentAmount == null ||
        currentCategory == null) {
        Log.e("TransactionMapper", "Cannot map TransactionApiResponse to TransactionItem due to missing essential fields. API Response ID: ${this.id ?: "N/A"}")
        return null
    }

    return TransactionItem(
        id = currentShortId,
        fullTransactionId = currentId,
        dateTime = parsedDateTime, // This is now non-null due to the check above
        accountId = currentAccountId,
        accountOwnerName = currentAccountOwnerName,
        amount = currentAmount,
        status = parsedStatus, // Already handled
        category = currentCategory,
        reasonForDecline = if (parsedDeclineReason is DeclineReason.None && (parsedStatus != TransactionStatus.DECLINED && parsedStatus != TransactionStatus.FAILED)) {
            null
        } else {
            parsedDeclineReason
        }
    )
}
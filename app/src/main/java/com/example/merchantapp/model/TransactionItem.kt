package com.example.merchantapp.model // This is its own package

import com.example.merchantapp.model.TransactionApiResponse // Assuming TransactionApiResponse is here
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.util.Log

// ... (rest of the enum, sealed class, data class, and mapper function from the previous response)
// NO CHANGE to the content of enums, data class, or mapper function itself, only the package statement at the top
// and the import of TransactionApiResponse.

enum class TransactionStatus {
    APPROVED,
    DECLINED,
    PENDING,
    FAILED
}

sealed class DeclineReason(val message: String) {
    object AccountNotFound : DeclineReason("Account Not Found")
    object AccountInactive : DeclineReason("Account Inactive")
    object IncorrectPin : DeclineReason("Incorrect PIN")
    object InsufficientBalance : DeclineReason("Insufficient Balance")
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
        Log.e("TransactionMapper", "Failed to parse date: ${this.dateTime}", e)
        null
    }

    val parsedStatus = when (this.status?.lowercase()) {
        "completed", "approved" -> TransactionStatus.APPROVED
        "pending" -> TransactionStatus.PENDING
        "declined" -> TransactionStatus.DECLINED
        "failed" -> TransactionStatus.FAILED
        else -> {
            Log.w("TransactionMapper", "Unknown transaction status from API: '${this.status}'")
            null
        }
    }

    val parsedDeclineReason = this.declineReason?.takeIf { it.isNotBlank() }?.let { reasonString ->
        when (reasonString.lowercase().trim()) {
            "account not found" -> DeclineReason.AccountNotFound
            "account inactive" -> DeclineReason.AccountInactive
            "incorrect pin" -> DeclineReason.IncorrectPin
            "insufficient balance" -> DeclineReason.InsufficientBalance
            else -> DeclineReason.Other(reasonString)
        }
    } ?: if (parsedStatus == TransactionStatus.DECLINED || parsedStatus == TransactionStatus.FAILED) {
        this.declineReason?.let { DeclineReason.Other(it) }
    } else {
        DeclineReason.None
    }

    if (this.id == null ||
        this.shortId == null ||
        parsedDateTime == null ||
        this.accountId == null ||
        this.accountOwnerName == null ||
        this.amount == null ||
        parsedStatus == null ||
        this.category == null) {
        Log.e("TransactionMapper", "Cannot map TransactionApiResponse to TransactionItem due to missing or invalid essential fields: $this")
        return null
    }

    return TransactionItem(
        id = this.shortId,
        fullTransactionId = this.id,
        dateTime = parsedDateTime,
        accountId = this.accountId,
        accountOwnerName = this.accountOwnerName,
        amount = this.amount,
        status = parsedStatus,
        category = this.category,
        reasonForDecline = if (parsedDeclineReason is DeclineReason.None && (parsedStatus != TransactionStatus.DECLINED && parsedStatus != TransactionStatus.FAILED)) null else parsedDeclineReason
    )
}
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val loanId: Long?,
    val transactionType: String, // "DISBURSEMENT_WITHDRAWAL", "LOAN_REPAYMENT", "LOAN_FUNDED"
    val channel: String, // "GCash", "Maya", "BDO Unibank", "BPI", "UnionBank", "Visa/Mastercard"
    val destinationType: String, // "E_WALLET", "BANK_TRANSFER", "PAYMENT_GATEWAY"
    val targetAccountEncrypted: String, // e.g. "•••• 8921 [ENC:AES-256]"
    val targetAccountName: String,
    val amount: Double,
    val fee: Double = 0.0,
    val netAmount: Double,
    val referenceNumber: String,
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val securityHash: String, // Simulated SHA-256 integrity token
    val timestamp: Long = System.currentTimeMillis()
)

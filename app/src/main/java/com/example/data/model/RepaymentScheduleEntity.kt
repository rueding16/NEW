package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repayment_schedules")
data class RepaymentScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val userId: Long,
    val installmentNumber: Int,
    val dueDateMillis: Long,
    val amountDue: Double,
    val penaltyAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paidDateMillis: Long? = null,
    val paymentMethod: String = "", // "CREDIT_DEBIT_CARD", "E_WALLET", "BANK_TRANSFER"
    val referenceNumber: String = "",
    val status: String = "PENDING" // "PENDING", "PAID", "OVERDUE"
)

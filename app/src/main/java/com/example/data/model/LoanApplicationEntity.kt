package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loan_applications")
data class LoanApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val customerName: String,
    val loanType: String, // "Personal Cash Loan", "Gadget & Phone Loan", "Home Appliance Loan", "Express Advance"
    val principalAmount: Double,
    val tenorMonths: Int,
    val monthlyInterestRate: Double, // e.g. 0.025 (2.5%)
    val monthlyInstallment: Double,
    val totalRepayment: Double,
    val purpose: String,
    val status: String = "PENDING_APPROVAL", // "PENDING_APPROVAL", "APPROVED", "REJECTED", "ACTIVE", "COMPLETED"
    val adminRemarks: String = "",
    val riskScore: Int = 650,
    val riskTier: String = "MODERATE", // "LOW", "MODERATE", "HIGH"
    val debtToIncomeRatio: Double = 0.25,
    val recommendedLimit: Double = 50000.0,
    val availableBalance: Double = 0.0, // Amount available for withdrawal once approved
    val disbursedTotal: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val appliedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val e2eHash: String = "" // Cryptographic tamper-proofing hash
)

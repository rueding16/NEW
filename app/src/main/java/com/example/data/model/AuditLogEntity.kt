package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actor: String,
    val action: String, // e.g., "LOAN_APPLICATION_SUBMITTED", "KYC_VERIFIED_BY_ADMIN", "LOAN_APPROVED", "REPAYMENT_PROCESSED", "BALANCE_DISBURSED"
    val details: String,
    val integrityHash: String,
    val timestamp: Long = System.currentTimeMillis()
)

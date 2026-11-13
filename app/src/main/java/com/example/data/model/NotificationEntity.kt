package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientRole: String, // "CUSTOMER", "ADMIN", "ALL"
    val targetUserId: Long? = null,
    val title: String,
    val message: String,
    val type: String, // "LOAN_STATUS", "KYC_STATUS", "REPAYMENT", "DISBURSEMENT", "SECURITY"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

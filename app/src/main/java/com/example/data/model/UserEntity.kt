package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String = "CUSTOMER", // "ADMIN" or "CUSTOMER"
    val kycStatus: String = "NOT_SUBMITTED", // "NOT_SUBMITTED", "PENDING_VERIFICATION", "VERIFIED", "REJECTED"
    val idType: String = "National ID", // "Passport", "Driver's License", "National ID", "UMID"
    val idNumberEncrypted: String = "",
    val employmentType: String = "Employed", // "Employed", "Self-Employed", "Business Owner", "Freelancer"
    val companyName: String = "",
    val monthlyIncome: Double = 0.0,
    val proofDocInfo: String = "",
    val registeredAt: Long = System.currentTimeMillis(),
    val kycRejectionReason: String = ""
)

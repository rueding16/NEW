package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_documents")
data class UserDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val docType: String, // "GOVERNMENT_ID", "PAY_SLIP", "BANK_STATEMENT", "EMPLOYMENT_PROOF"
    val title: String,
    val fileName: String,
    val fileSizeKb: Int,
    val mimeType: String = "image/jpeg",
    val status: String = "VERIFIED", // "VERIFIED", "IN_REVIEW", "REJECTED"
    val uploadedAt: Long = System.currentTimeMillis(),
    val storagePath: String = "secure://users/documents",
    val integrityHash: String = "SHA256_SEC"
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "communication_messages")
data class CommunicationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val channel: String, // "SMS", "EMAIL", "APP_PUSH"
    val recipient: String, // Phone number or email
    val subject: String,
    val body: String,
    val gateway: String, // "Twilio SMS Gateway", "SendGrid Email API", "FCM Push"
    val status: String = "DELIVERED", // "DELIVERED", "SENT", "PENDING"
    val externalMessageId: String = "MSG_EXT",
    val sentAt: Long = System.currentTimeMillis()
)

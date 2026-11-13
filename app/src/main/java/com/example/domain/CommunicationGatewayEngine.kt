package com.example.domain

import com.example.data.model.CommunicationMessageEntity

object CommunicationGatewayEngine {

    fun createSmsAlert(
        userId: Long,
        phone: String,
        body: String
    ): CommunicationMessageEntity {
        val extId = "TWILIO-SM" + System.currentTimeMillis().toString().takeLast(10)
        return CommunicationMessageEntity(
            userId = userId,
            channel = "SMS",
            recipient = phone,
            subject = "CrediPulse SMS Alert",
            body = body,
            gateway = "Twilio SMS Gateway (+1 888-555-LOAN)",
            status = "DELIVERED",
            externalMessageId = extId,
            sentAt = System.currentTimeMillis()
        )
    }

    fun createEmailReceipt(
        userId: Long,
        email: String,
        subject: String,
        body: String
    ): CommunicationMessageEntity {
        val extId = "SENDGRID-SG" + System.currentTimeMillis().toString().takeLast(10)
        return CommunicationMessageEntity(
            userId = userId,
            channel = "EMAIL",
            recipient = email,
            subject = subject,
            body = body,
            gateway = "SendGrid Transactional API (receipts@credipulse.com)",
            status = "DELIVERED",
            externalMessageId = extId,
            sentAt = System.currentTimeMillis()
        )
    }

    fun createAppPush(
        userId: Long,
        title: String,
        body: String
    ): CommunicationMessageEntity {
        val extId = "FCM-PUSH-" + System.currentTimeMillis().toString().takeLast(10)
        return CommunicationMessageEntity(
            userId = userId,
            channel = "APP_PUSH",
            recipient = "Device Token (Android API 36)",
            subject = title,
            body = body,
            gateway = "Firebase Cloud Messaging (FCM)",
            status = "DELIVERED",
            externalMessageId = extId,
            sentAt = System.currentTimeMillis()
        )
    }
}

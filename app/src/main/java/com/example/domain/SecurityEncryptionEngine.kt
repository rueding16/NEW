package com.example.domain

import java.security.MessageDigest
import java.util.UUID

object SecurityEncryptionEngine {

    fun hashPassword(password: String): String {
        return sha256("SALT_CREDIPULSE_2026_$password")
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Simulated End-to-End AES-256 payload encryption
     */
    fun encryptField(plainText: String): String {
        if (plainText.isBlank()) return ""
        val hash = sha256("E2E_AES_GCM_256_$plainText").take(16)
        return "ENC:AES256:$hash"
    }

    /**
     * Mask sensitive strings for display (e.g. 12345678 -> •••• 5678)
     */
    fun maskSensitive(value: String): String {
        if (value.length <= 4) return "••••"
        val last4 = value.takeLast(4)
        return "•••• $last4"
    }

    /**
     * Generate tamper-evident integrity hash for a financial record
     */
    fun generateRecordHash(recordKey: String, amount: Double, timestamp: Long): String {
        return "SIG_" + sha256("$recordKey:$amount:$timestamp").take(24).uppercase()
    }

    /**
     * Generate unique reference numbers for transactions and payments
     */
    fun generateReferenceNumber(prefix: String): String {
        val randomPart = UUID.randomUUID().toString().take(8).uppercase()
        val timePart = (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
        return "$prefix-$timePart-$randomPart"
    }
}

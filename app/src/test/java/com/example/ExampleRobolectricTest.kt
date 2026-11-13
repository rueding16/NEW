package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.CreditScoringEngine
import com.example.domain.SecurityEncryptionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CrediPulse", appName)
    }

    @Test
    fun `verify security password hashing and validation`() {
        val password = "@adminpass2026"
        val hash = SecurityEncryptionEngine.hashPassword(password)
        assertTrue(SecurityEncryptionEngine.verifyPassword(password, hash))
        assertFalse(SecurityEncryptionEngine.verifyPassword("wrongpass", hash))
    }

    @Test
    fun `verify data masking and record hash generation`() {
        val masked = SecurityEncryptionEngine.maskSensitive("09171234567")
        assertTrue(masked.contains("4567"))
        assertTrue(masked.startsWith("••••"))

        val recordHash = SecurityEncryptionEngine.generateRecordHash("TXN-TEST", 25000.0, 1000L)
        assertNotNull(recordHash)
        assertTrue(recordHash.startsWith("SIG_"))
    }

    @Test
    fun `verify credit scoring and automated risk assessment`() {
        // High income, verified KYC, low DTI -> Low risk
        val primeAssessment = CreditScoringEngine.calculateRiskAssessment(
            monthlyIncome = 85000.0,
            requestedAmount = 20000.0,
            tenorMonths = 12,
            employmentType = "Employed",
            kycStatus = "VERIFIED",
            completedLoansCount = 1
        )

        assertEquals("LOW", primeAssessment.tier)
        assertTrue(primeAssessment.score >= 700)
        assertTrue(primeAssessment.recommendedLimit > 50000.0)

        // Low income, rejected KYC, high loan -> High risk
        val highRiskAssessment = CreditScoringEngine.calculateRiskAssessment(
            monthlyIncome = 15000.0,
            requestedAmount = 80000.0,
            tenorMonths = 6,
            employmentType = "Freelancer",
            kycStatus = "REJECTED",
            overdueCount = 2
        )

        assertEquals("HIGH", highRiskAssessment.tier)
        assertTrue(highRiskAssessment.score < 600)
    }
}

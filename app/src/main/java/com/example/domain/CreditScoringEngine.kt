package com.example.domain

data class CreditRiskAssessment(
    val score: Int, // 300 - 850
    val tier: String, // "LOW", "MODERATE", "HIGH"
    val tierLabel: String,
    val debtToIncomeRatio: Double,
    val recommendedLimit: Double,
    val recommendedInterestRate: Double,
    val summary: String,
    val breakdownFactors: List<ScoreFactor>
)

data class ScoreFactor(
    val title: String,
    val impact: String, // "+50 pts", "-30 pts", etc.
    val isPositive: Boolean,
    val description: String
)

object CreditScoringEngine {

    fun calculateRiskAssessment(
        monthlyIncome: Double,
        requestedAmount: Double,
        tenorMonths: Int,
        employmentType: String,
        kycStatus: String,
        existingDebtPayments: Double = 0.0,
        completedLoansCount: Int = 0,
        overdueCount: Int = 0
    ): CreditRiskAssessment {
        var baseScore = 550
        val factors = mutableListOf<ScoreFactor>()

        // 1. Income Factor
        when {
            monthlyIncome >= 80000 -> {
                baseScore += 90
                factors.add(ScoreFactor("High Income Bracket", "+90 pts", true, "Verified income above ₱80,000/mo demonstrates strong capacity"))
            }
            monthlyIncome >= 40000 -> {
                baseScore += 60
                factors.add(ScoreFactor("Stable Income", "+60 pts", true, "Income between ₱40k - ₱80k provides adequate safety margin"))
            }
            monthlyIncome >= 20000 -> {
                baseScore += 30
                factors.add(ScoreFactor("Entry Income", "+30 pts", true, "Income meets standard eligibility threshold"))
            }
            else -> {
                baseScore -= 40
                factors.add(ScoreFactor("Low Monthly Income", "-40 pts", false, "Monthly income under ₱20,000 increases default vulnerability"))
            }
        }

        // 2. Employment Stability
        when (employmentType) {
            "Employed" -> {
                baseScore += 50
                factors.add(ScoreFactor("Corporate Employment", "+50 pts", true, "Salaried employment provides consistent cashflow"))
            }
            "Business Owner" -> {
                baseScore += 45
                factors.add(ScoreFactor("Business Ownership", "+45 pts", true, "Entrepreneurial revenue streams"))
            }
            "Self-Employed" -> {
                baseScore += 25
                factors.add(ScoreFactor("Self-Employed", "+25 pts", true, "Self-directed freelance / consultancy earnings"))
            }
            else -> {
                baseScore += 10
                factors.add(ScoreFactor("Other Employment", "+10 pts", true, "General income source"))
            }
        }

        // 3. KYC Status
        when (kycStatus) {
            "VERIFIED" -> {
                baseScore += 80
                factors.add(ScoreFactor("KYC Verified by Admin", "+80 pts", true, "Full identity and income documents verified by compliance"))
            }
            "PENDING_VERIFICATION" -> {
                baseScore += 20
                factors.add(ScoreFactor("KYC In Review", "+20 pts", true, "Documents submitted and pending admin review"))
            }
            "REJECTED" -> {
                baseScore -= 80
                factors.add(ScoreFactor("KYC Rejected", "-80 pts", false, "Identity or proof document verification failed"))
            }
            else -> {
                baseScore -= 30
                factors.add(ScoreFactor("Unverified KYC", "-30 pts", false, "No valid government ID on file"))
            }
        }

        // 4. Estimated Monthly Installment & DTI
        val monthlyInterest = 0.025
        val monthlyDue = if (tenorMonths > 0) {
            (requestedAmount * (1 + (monthlyInterest * tenorMonths))) / tenorMonths
        } else requestedAmount

        val totalMonthlyDebt = existingDebtPayments + monthlyDue
        val safeIncome = if (monthlyIncome <= 0) 1000.0 else monthlyIncome
        val dti = totalMonthlyDebt / safeIncome

        when {
            dti <= 0.25 -> {
                baseScore += 60
                factors.add(ScoreFactor("Optimal Debt-to-Income (DTI: ${(dti * 100).toInt()}%)", "+60 pts", true, "Loan installment is well below 25% of earnings"))
            }
            dti <= 0.40 -> {
                baseScore += 25
                factors.add(ScoreFactor("Moderate DTI (${(dti * 100).toInt()}%)", "+25 pts", true, "Installment represents reasonable share of income"))
            }
            dti <= 0.55 -> {
                baseScore -= 30
                factors.add(ScoreFactor("Elevated DTI (${(dti * 100).toInt()}%)", "-30 pts", false, "High debt obligations may stress repayments"))
            }
            else -> {
                baseScore -= 70
                factors.add(ScoreFactor("Critical DTI (${(dti * 100).toInt()}%)", "-70 pts", false, "Installment exceeds 55% of monthly income"))
            }
        }

        // 5. Repayment History
        if (completedLoansCount > 0) {
            val boost = (completedLoansCount * 25).coerceAtMost(75)
            baseScore += boost
            factors.add(ScoreFactor("Successful Loan Repayments", "+$boost pts", true, "$completedLoansCount previous loan(s) settled in full"))
        }

        if (overdueCount > 0) {
            val penalty = overdueCount * 50
            baseScore -= penalty
            factors.add(ScoreFactor("Delinquency History", "-$penalty pts", false, "$overdueCount overdue record(s) flagged"))
        }

        val finalScore = baseScore.coerceIn(300, 850)

        val (tier, tierLabel, interestRate) = when {
            finalScore >= 720 -> Triple("LOW", "Low Risk (Prime Borrower)", 0.020)
            finalScore >= 600 -> Triple("MODERATE", "Moderate Risk (Standard)", 0.028)
            else -> Triple("HIGH", "High Risk (Conditional)", 0.038)
        }

        val recommendedLimit = when (tier) {
            "LOW" -> (monthlyIncome * 3.5).coerceIn(30000.0, 150000.0)
            "MODERATE" -> (monthlyIncome * 2.0).coerceIn(15000.0, 60000.0)
            else -> (monthlyIncome * 1.0).coerceIn(5000.0, 20000.0)
        }

        val summary = when (tier) {
            "LOW" -> "Excellent credit standing. High approval probability with prime interest rate and maximum borrowing limit."
            "MODERATE" -> "Satisfactory risk profile. Standard interest rates apply. Admin review recommended to ensure prompt disbursement."
            else -> "High risk profile detected. Requires verified KYC, lower borrowing amount, or additional security before approval."
        }

        return CreditRiskAssessment(
            score = finalScore,
            tier = tier,
            tierLabel = tierLabel,
            debtToIncomeRatio = dti,
            recommendedLimit = recommendedLimit,
            recommendedInterestRate = interestRate,
            summary = summary,
            breakdownFactors = factors
        )
    }
}

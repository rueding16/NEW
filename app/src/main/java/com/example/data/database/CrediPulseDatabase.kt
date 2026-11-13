package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AuditLogDao
import com.example.data.dao.CommunicationMessageDao
import com.example.data.dao.LoanDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.RepaymentDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserDao
import com.example.data.dao.UserDocumentDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.CommunicationMessageEntity
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.RepaymentScheduleEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserDocumentEntity
import com.example.data.model.UserEntity
import com.example.domain.SecurityEncryptionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        LoanApplicationEntity::class,
        RepaymentScheduleEntity::class,
        TransactionEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        UserDocumentEntity::class,
        CommunicationMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CrediPulseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userDocumentDao(): UserDocumentDao
    abstract fun communicationMessageDao(): CommunicationMessageDao

    companion object {
        @Volatile
        private var INSTANCE: CrediPulseDatabase? = null

        fun getInstance(context: Context): CrediPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrediPulseDatabase::class.java,
                    "credipulse_loan_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default admin and demo data
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedInitialData(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: CrediPulseDatabase) {
            val userDao = database.userDao()
            val loanDao = database.loanDao()
            val notificationDao = database.notificationDao()
            val auditLogDao = database.auditLogDao()

            // 1. Mandatory Admin Account
            val adminUser = UserEntity(
                username = "admin",
                passwordHash = SecurityEncryptionEngine.hashPassword("@adminpass2026"),
                fullName = "Chief Risk Officer (Admin)",
                email = "admin@credipulse.com",
                phone = "+63 917 888 2026",
                role = "ADMIN",
                kycStatus = "VERIFIED",
                idType = "National ID",
                idNumberEncrypted = SecurityEncryptionEngine.encryptField("ADM-2026-9999"),
                employmentType = "Executive",
                companyName = "CrediPulse Financial Corp.",
                monthlyIncome = 150000.0
            )
            userDao.insertUser(adminUser)

            // 2. Demo Customer Account for immediate testing
            val customerUser = UserEntity(
                username = "maria.santos",
                passwordHash = SecurityEncryptionEngine.hashPassword("maria123"),
                fullName = "Maria Clara Santos",
                email = "maria.santos@email.ph",
                phone = "+63 928 555 1234",
                role = "CUSTOMER",
                kycStatus = "PENDING_VERIFICATION",
                idType = "Driver's License",
                idNumberEncrypted = SecurityEncryptionEngine.encryptField("N02-18-987654"),
                employmentType = "Employed",
                companyName = "Accenture Philippines Inc.",
                monthlyIncome = 45000.0,
                proofDocInfo = "Latest 3-Month Payslip & ITR 2316"
            )
            val customerId = userDao.insertUser(customerUser)

            // 3. Demo Loan Application pending admin review
            val principal = 35000.0
            val tenor = 6
            val interestRate = 0.028
            val totalRepayment = principal * (1 + (interestRate * tenor))
            val monthlyDue = totalRepayment / tenor

            val demoLoan = LoanApplicationEntity(
                userId = customerId,
                customerName = "Maria Clara Santos",
                loanType = "Gadget & Phone Loan",
                principalAmount = principal,
                tenorMonths = tenor,
                monthlyInterestRate = interestRate,
                monthlyInstallment = monthlyDue,
                totalRepayment = totalRepayment,
                purpose = "Laptop purchase for remote software development work",
                status = "PENDING_APPROVAL",
                adminRemarks = "System assessment complete. Pending admin review & KYC approval.",
                riskScore = 680,
                riskTier = "MODERATE",
                debtToIncomeRatio = monthlyDue / 45000.0,
                recommendedLimit = 75000.0,
                availableBalance = 0.0,
                disbursedTotal = 0.0,
                remainingBalance = totalRepayment,
                e2eHash = SecurityEncryptionEngine.generateRecordHash("LOAN-$customerId", principal, System.currentTimeMillis())
            )
            loanDao.insertLoan(demoLoan)

            // 4. Initial System Notifications
            notificationDao.insertNotification(
                NotificationEntity(
                    recipientRole = "ADMIN",
                    title = "New Loan Application",
                    message = "Maria Clara Santos submitted a loan application for ₱35,000 (Gadget & Phone Loan).",
                    type = "LOAN_STATUS"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    recipientRole = "ADMIN",
                    title = "Pending KYC Verification",
                    message = "Maria Clara Santos uploaded Driver's License and proof of income for verification.",
                    type = "KYC_STATUS"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    recipientRole = "CUSTOMER",
                    targetUserId = customerId,
                    title = "Application Submitted",
                    message = "Your loan application for ₱35,000 has been securely submitted. Admin review in progress.",
                    type = "LOAN_STATUS"
                )
            )

            // 5. Initial Audit Log
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    actor = "SYSTEM_INITIALIZER",
                    action = "SYSTEM_BOOTSTRAP",
                    details = "CrediPulse secure ledger initialized with E2E AES-256 encryption & admin account @adminpass2026.",
                    integrityHash = SecurityEncryptionEngine.sha256("BOOTSTRAP_${System.currentTimeMillis()}")
                )
            )
        }
    }
}

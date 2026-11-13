package com.example.data.repository

import com.example.data.database.CrediPulseDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CommunicationMessageEntity
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.RepaymentScheduleEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserDocumentEntity
import com.example.data.model.UserEntity
import com.example.domain.CommunicationGatewayEngine
import com.example.domain.CreditScoringEngine
import com.example.domain.SecurityEncryptionEngine
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class CrediPulseRepository(private val database: CrediPulseDatabase) {
    private val userDao = database.userDao()
    private val loanDao = database.loanDao()
    private val repaymentDao = database.repaymentDao()
    private val transactionDao = database.transactionDao()
    private val notificationDao = database.notificationDao()
    private val auditLogDao = database.auditLogDao()
    private val userDocumentDao = database.userDocumentDao()
    private val messageDao = database.communicationMessageDao()

    suspend fun ensureDefaultAdmin() {
        val admin = userDao.getUserByUsername("admin")
        if (admin == null) {
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
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun getUserByUsername(username: String): UserEntity? = userDao.getUserByUsername(username)

    suspend fun login(username: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByUsername(username.trim())
            ?: return Result.failure(Exception("User not found with username '$username'"))

        val isMatched = SecurityEncryptionEngine.verifyPassword(password, user.passwordHash)
        return if (isMatched) {
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    actor = user.username,
                    action = "AUTH_LOGIN_SUCCESS",
                    details = "User ${user.username} (${user.role}) authenticated successfully.",
                    integrityHash = SecurityEncryptionEngine.sha256("LOGIN_${user.id}_${System.currentTimeMillis()}")
                )
            )
            Result.success(user)
        } else {
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    actor = username,
                    action = "AUTH_LOGIN_FAILED",
                    details = "Failed authentication attempt for username '$username'.",
                    integrityHash = SecurityEncryptionEngine.sha256("FAIL_LOGIN_${System.currentTimeMillis()}")
                )
            )
            Result.failure(Exception("Invalid password. Please verify credentials."))
        }
    }

    suspend fun registerCustomer(
        username: String,
        password: String,
        fullName: String,
        email: String,
        phone: String,
        role: String = "CUSTOMER",
        idType: String,
        idNumber: String,
        employmentType: String,
        companyName: String,
        monthlyIncome: Double,
        proofDocInfo: String
    ): Result<UserEntity> {
        val existing = userDao.getUserByUsername(username.trim())
        if (existing != null) {
            return Result.failure(Exception("Username '$username' is already taken."))
        }

        val encryptedId = SecurityEncryptionEngine.encryptField(idNumber)
        val user = UserEntity(
            username = username.trim(),
            passwordHash = SecurityEncryptionEngine.hashPassword(password),
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            role = role,
            kycStatus = if (role == "ADMIN") "VERIFIED" else "PENDING_VERIFICATION",
            idType = idType,
            idNumberEncrypted = encryptedId,
            employmentType = employmentType,
            companyName = companyName.trim(),
            monthlyIncome = monthlyIncome,
            proofDocInfo = proofDocInfo.trim()
        )

        val newId = userDao.insertUser(user)
        val createdUser = user.copy(id = newId)

        // Audit Log
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = username,
                action = "USER_REGISTERED",
                details = "Registered user '$username' role: $role with encrypted credentials.",
                integrityHash = SecurityEncryptionEngine.sha256("REG_$newId")
            )
        )

        // Notifications
        if (role == "CUSTOMER") {
            // Seed initial per-account document vault records
            userDocumentDao.insertDocument(
                UserDocumentEntity(
                    userId = newId,
                    docType = "GOVERNMENT_ID",
                    title = "$idType (Primary Identity)",
                    fileName = "${idType.lowercase().replace(" ", "_")}_front.jpg",
                    fileSizeKb = 840,
                    status = "IN_REVIEW",
                    integrityHash = SecurityEncryptionEngine.sha256("DOC_ID_$newId")
                )
            )
            if (proofDocInfo.isNotBlank()) {
                userDocumentDao.insertDocument(
                    UserDocumentEntity(
                        userId = newId,
                        docType = "PAY_SLIP",
                        title = "Income & Employment Proof",
                        fileName = "income_proof_${System.currentTimeMillis().toString().takeLast(6)}.pdf",
                        fileSizeKb = 1420,
                        mimeType = "application/pdf",
                        status = "IN_REVIEW",
                        integrityHash = SecurityEncryptionEngine.sha256("DOC_INC_$newId")
                    )
                )
            }

            // Multi-channel welcome alerts (Twilio SMS, SendGrid Email, FCM Push)
            messageDao.insertMessage(
                CommunicationGatewayEngine.createSmsAlert(
                    userId = newId,
                    phone = phone,
                    body = "CrediPulse SMS: Welcome $fullName! Your account is active. SMS alerts will be sent here via Twilio Gateway."
                )
            )
            messageDao.insertMessage(
                CommunicationGatewayEngine.createEmailReceipt(
                    userId = newId,
                    email = email,
                    subject = "Welcome to CrediPulse Financial Corp.",
                    body = "Dear $fullName, your account has been encrypted & initialized. Review your credit limit and submit loan applications anytime."
                )
            )
            messageDao.insertMessage(
                CommunicationGatewayEngine.createAppPush(
                    userId = newId,
                    title = "Account Initialized",
                    body = "Welcome to CrediPulse! KYC documents uploaded to your secure per-account vault."
                )
            )

            notificationDao.insertNotification(
                NotificationEntity(
                    recipientRole = "ADMIN",
                    title = "New Customer Registered",
                    message = "Customer $fullName ($username) registered and submitted KYC documents for approval.",
                    type = "KYC_STATUS"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    recipientRole = "CUSTOMER",
                    targetUserId = newId,
                    title = "Welcome to CrediPulse!",
                    message = "Your profile was encrypted and created. Submit a loan application anytime.",
                    type = "SECURITY"
                )
            )
        }

        return Result.success(createdUser)
    }

    fun observeUser(userId: Long): Flow<UserEntity?> = userDao.observeUserById(userId)

    fun getAllCustomers(): Flow<List<UserEntity>> = userDao.getAllCustomers()

    suspend fun updateKycStatus(adminUsername: String, userId: Long, status: String, reason: String) {
        userDao.updateKycStatus(userId, status, reason)
        val customer = userDao.getUserById(userId)
        val customerName = customer?.fullName ?: "Customer #$userId"

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = adminUsername,
                action = "KYC_STATUS_UPDATED",
                details = "Admin $adminUsername updated KYC status of $customerName to $status. Reason: $reason",
                integrityHash = SecurityEncryptionEngine.sha256("KYC_$userId:$status")
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "CUSTOMER",
                targetUserId = userId,
                title = if (status == "VERIFIED") "KYC Profile Verified! ✅" else "KYC Review Update ⚠️",
                message = if (status == "VERIFIED") {
                    "Your identity and income details have been approved by CrediPulse compliance. You are eligible for enhanced credit limits."
                } else {
                    "Your KYC details were marked as $status. Reason: $reason"
                },
                type = "KYC_STATUS"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "ADMIN",
                title = "KYC Review Completed",
                message = "Admin $adminUsername updated $customerName KYC to $status.",
                type = "KYC_STATUS"
            )
        )
    }

    suspend fun applyForLoan(
        user: UserEntity,
        loanType: String,
        principalAmount: Double,
        tenorMonths: Int,
        purpose: String
    ): Result<Long> {
        val risk = CreditScoringEngine.calculateRiskAssessment(
            monthlyIncome = user.monthlyIncome,
            requestedAmount = principalAmount,
            tenorMonths = tenorMonths,
            employmentType = user.employmentType,
            kycStatus = user.kycStatus
        )

        val interestRate = risk.recommendedInterestRate
        val totalRepayment = principalAmount * (1 + (interestRate * tenorMonths))
        val monthlyDue = totalRepayment / tenorMonths

        val e2eHash = SecurityEncryptionEngine.generateRecordHash(
            recordKey = "LOAN_${user.id}_$loanType",
            amount = principalAmount,
            timestamp = System.currentTimeMillis()
        )

        val loan = LoanApplicationEntity(
            userId = user.id,
            customerName = user.fullName,
            loanType = loanType,
            principalAmount = principalAmount,
            tenorMonths = tenorMonths,
            monthlyInterestRate = interestRate,
            monthlyInstallment = monthlyDue,
            totalRepayment = totalRepayment,
            purpose = purpose.trim(),
            status = "PENDING_APPROVAL",
            adminRemarks = "Automated Risk Scoring: Score ${risk.score} (${risk.tier}). Pending Admin Approval.",
            riskScore = risk.score,
            riskTier = risk.tier,
            debtToIncomeRatio = risk.debtToIncomeRatio,
            recommendedLimit = risk.recommendedLimit,
            availableBalance = 0.0,
            disbursedTotal = 0.0,
            remainingBalance = totalRepayment,
            e2eHash = e2eHash
        )

        val loanId = loanDao.insertLoan(loan)

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = user.username,
                action = "LOAN_APPLICATION_SUBMITTED",
                details = "Applied for $loanType of ₱$principalAmount for $tenorMonths months. Risk score: ${risk.score} (${risk.tier}).",
                integrityHash = e2eHash
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "ADMIN",
                title = "New Loan Pending Approval",
                message = "${user.fullName} applied for ₱$principalAmount ($loanType). Risk Tier: ${risk.tier} (${risk.score} pts).",
                type = "LOAN_STATUS"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "CUSTOMER",
                targetUserId = user.id,
                title = "Loan Application Submitted",
                message = "Your ₱$principalAmount loan application has been received. Our credit officers are evaluating your submission in real-time.",
                type = "LOAN_STATUS"
            )
        )

        // Multi-Channel Messaging: Twilio SMS, SendGrid Email, FCM Push
        messageDao.insertMessage(
            CommunicationGatewayEngine.createSmsAlert(
                userId = user.id,
                phone = user.phone,
                body = "CrediPulse SMS: Your application for ₱$principalAmount ($loanType) is received. Under real-time underwriting."
            )
        )
        messageDao.insertMessage(
            CommunicationGatewayEngine.createEmailReceipt(
                userId = user.id,
                email = user.email,
                subject = "Loan Application Received - CrediPulse #$loanId",
                body = "Dear ${user.fullName}, we received your application for ₱$principalAmount for $tenorMonths months. Risk Tier: ${risk.tier}."
            )
        )
        messageDao.insertMessage(
            CommunicationGatewayEngine.createAppPush(
                userId = user.id,
                title = "Loan Application In Review",
                body = "₱$principalAmount loan application is under compliance review."
            )
        )

        return Result.success(loanId)
    }

    fun getAllLoans(): Flow<List<LoanApplicationEntity>> = loanDao.getAllLoans()

    fun getLoansForUser(userId: Long): Flow<List<LoanApplicationEntity>> = loanDao.getLoansByUserId(userId)

    suspend fun approveLoan(adminUsername: String, loanId: Long, remarks: String) {
        val loan = loanDao.getLoanById(loanId) ?: return
        val approvedAt = System.currentTimeMillis()

        // Update loan status to APPROVED and grant available balance
        loanDao.updateLoanStatus(
            loanId = loanId,
            status = "APPROVED",
            remarks = if (remarks.isBlank()) "Approved by Administrator $adminUsername." else remarks,
            approvedAt = approvedAt,
            availableBalance = loan.principalAmount,
            remainingBalance = loan.totalRepayment
        )

        // Generate repayment installment schedules
        val schedules = mutableListOf<RepaymentScheduleEntity>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = approvedAt

        for (i in 1..loan.tenorMonths) {
            cal.add(Calendar.MONTH, 1)
            schedules.add(
                RepaymentScheduleEntity(
                    loanId = loanId,
                    userId = loan.userId,
                    installmentNumber = i,
                    dueDateMillis = cal.timeInMillis,
                    amountDue = loan.monthlyInstallment,
                    status = "PENDING"
                )
            )
        }
        repaymentDao.insertSchedules(schedules)

        // Audit Log
        val hash = SecurityEncryptionEngine.generateRecordHash("APPROVE_$loanId", loan.principalAmount, approvedAt)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = adminUsername,
                action = "LOAN_APPROVED",
                details = "Admin $adminUsername approved Loan #$loanId of ₱${loan.principalAmount} for ${loan.customerName}.",
                integrityHash = hash
            )
        )

        // Notifications
        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "CUSTOMER",
                targetUserId = loan.userId,
                title = "Congratulations! Loan Approved 🎉",
                message = "Your loan of ₱${loan.principalAmount} is approved! ₱${loan.principalAmount} is now in your Available Balance ready for E-Wallet or Bank withdrawal.",
                type = "LOAN_STATUS"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "ADMIN",
                title = "Loan Approval Executed",
                message = "Admin $adminUsername approved Loan #$loanId (₱${loan.principalAmount}) for ${loan.customerName}.",
                type = "LOAN_STATUS"
            )
        )

        val customer = userDao.getUserById(loan.userId)
        if (customer != null) {
            messageDao.insertMessage(
                CommunicationGatewayEngine.createSmsAlert(
                    userId = loan.userId,
                    phone = customer.phone,
                    body = "CrediPulse ALERT: Congratulations ${customer.fullName}! Loan #${loan.id} of ₱${loan.principalAmount} is APPROVED. Funds are now available for withdrawal."
                )
            )
            messageDao.insertMessage(
                CommunicationGatewayEngine.createEmailReceipt(
                    userId = loan.userId,
                    email = customer.email,
                    subject = "Official Approval: Loan #${loan.id} Approved",
                    body = "Congratulations ${customer.fullName}! Your loan of ₱${loan.principalAmount} has been approved. Repayment schedule is now active."
                )
            )
            messageDao.insertMessage(
                CommunicationGatewayEngine.createAppPush(
                    userId = loan.userId,
                    title = "Loan Approved! 🎉",
                    body = "₱${loan.principalAmount} credited to your available balance."
                )
            )
        }
    }

    suspend fun rejectLoan(adminUsername: String, loanId: Long, remarks: String) {
        val loan = loanDao.getLoanById(loanId) ?: return
        val now = System.currentTimeMillis()

        loanDao.updateLoanStatus(
            loanId = loanId,
            status = "REJECTED",
            remarks = if (remarks.isBlank()) "Declined per credit risk policy." else remarks,
            approvedAt = null,
            availableBalance = 0.0,
            remainingBalance = 0.0
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = adminUsername,
                action = "LOAN_REJECTED",
                details = "Admin $adminUsername rejected Loan #$loanId for ${loan.customerName}. Reason: $remarks",
                integrityHash = SecurityEncryptionEngine.sha256("REJECT_$loanId:$now")
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "CUSTOMER",
                targetUserId = loan.userId,
                title = "Loan Application Update",
                message = "Your loan application #$loanId could not be approved at this time. Remarks: $remarks",
                type = "LOAN_STATUS"
            )
        )
    }

    suspend fun withdrawAvailableBalance(
        user: UserEntity,
        loanId: Long,
        amount: Double,
        channel: String,
        destinationType: String,
        targetAccount: String,
        targetAccountName: String
    ): Result<TransactionEntity> {
        val loan = loanDao.getLoanById(loanId)
            ?: return Result.failure(Exception("Loan not found"))

        if (loan.availableBalance < amount) {
            return Result.failure(Exception("Insufficient available balance. Available: ₱${loan.availableBalance}"))
        }

        loanDao.deductAvailableBalance(loanId, amount)
        // If loan was APPROVED, transition to ACTIVE as funds are being disbursed
        if (loan.status == "APPROVED") {
            loanDao.updateLoan(loan.copy(status = "ACTIVE", availableBalance = loan.availableBalance - amount, disbursedTotal = loan.disbursedTotal + amount))
        }

        val ref = SecurityEncryptionEngine.generateReferenceNumber(if (destinationType == "E_WALLET") "EWL" else "BNK")
        val encryptedAccount = "${SecurityEncryptionEngine.maskSensitive(targetAccount)} [${SecurityEncryptionEngine.encryptField(targetAccount)}]"
        val securityHash = SecurityEncryptionEngine.generateRecordHash(ref, amount, System.currentTimeMillis())

        val transaction = TransactionEntity(
            userId = user.id,
            loanId = loanId,
            transactionType = "DISBURSEMENT_WITHDRAWAL",
            channel = channel,
            destinationType = destinationType,
            targetAccountEncrypted = encryptedAccount,
            targetAccountName = targetAccountName.trim(),
            amount = amount,
            fee = 0.0,
            netAmount = amount,
            referenceNumber = ref,
            status = "SUCCESS",
            securityHash = securityHash
        )

        val txId = transactionDao.insertTransaction(transaction)

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = user.username,
                action = "BALANCE_WITHDRAWN",
                details = "Transferred ₱$amount via $channel to $targetAccountName ($encryptedAccount). Ref: $ref",
                integrityHash = securityHash
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "CUSTOMER",
                targetUserId = user.id,
                title = "Funds Disbursed Successfully! 💸",
                message = "₱$amount has been transferred to your $channel account ($targetAccountName). Ref: $ref.",
                type = "DISBURSEMENT"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "ADMIN",
                title = "Customer Balance Withdrawn",
                message = "${user.fullName} withdrew ₱$amount via $channel. Ref: $ref.",
                type = "DISBURSEMENT"
            )
        )

        messageDao.insertMessage(
            CommunicationGatewayEngine.createSmsAlert(
                userId = user.id,
                phone = user.phone,
                body = "CrediPulse PAYOUT: ₱$amount sent via $channel to $targetAccountName ($targetAccount). Ref: $ref."
            )
        )
        messageDao.insertMessage(
            CommunicationGatewayEngine.createEmailReceipt(
                userId = user.id,
                email = user.email,
                subject = "Disbursement Receipt - $ref",
                body = "Dear ${user.fullName}, ₱$amount was disbursed to $targetAccountName via $channel. Reference: $ref."
            )
        )
        messageDao.insertMessage(
            CommunicationGatewayEngine.createAppPush(
                userId = user.id,
                title = "Payout Disbursed 💸",
                body = "₱$amount sent via $channel."
            )
        )

        return Result.success(transaction.copy(id = txId))
    }

    suspend fun processRepayment(
        user: UserEntity,
        scheduleId: Long,
        paymentMethod: String,
        channelName: String,
        accountIdentifier: String,
        amountToPay: Double
    ): Result<TransactionEntity> {
        val schedule = repaymentDao.getScheduleById(scheduleId)
            ?: return Result.failure(Exception("Installment schedule not found"))

        val now = System.currentTimeMillis()
        val ref = SecurityEncryptionEngine.generateReferenceNumber("PAY")
        val encryptedAccount = "${SecurityEncryptionEngine.maskSensitive(accountIdentifier)} [${SecurityEncryptionEngine.encryptField(accountIdentifier)}]"
        val securityHash = SecurityEncryptionEngine.generateRecordHash(ref, amountToPay, now)

        // Mark schedule as paid
        repaymentDao.markAsPaid(
            id = scheduleId,
            paidAmount = amountToPay,
            paidDate = now,
            method = paymentMethod,
            ref = ref
        )

        // Deduct remaining balance of the loan
        loanDao.deductRemainingBalance(schedule.loanId, amountToPay)

        // Check if all schedules are paid
        val loan = loanDao.getLoanById(schedule.loanId)
        if (loan != null && (loan.remainingBalance - amountToPay) <= 1.0) {
            loanDao.updateLoan(loan.copy(status = "COMPLETED", remainingBalance = 0.0))
        }

        val transaction = TransactionEntity(
            userId = user.id,
            loanId = schedule.loanId,
            transactionType = "LOAN_REPAYMENT",
            channel = channelName,
            destinationType = "PAYMENT_GATEWAY",
            targetAccountEncrypted = encryptedAccount,
            targetAccountName = user.fullName,
            amount = amountToPay,
            fee = 0.0,
            netAmount = amountToPay,
            referenceNumber = ref,
            status = "SUCCESS",
            securityHash = securityHash
        )

        val txId = transactionDao.insertTransaction(transaction)

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = user.username,
                action = "LOAN_REPAYMENT_PROCESSED",
                details = "Repayment of ₱$amountToPay processed for Installment #${schedule.installmentNumber} via $channelName. Ref: $ref",
                integrityHash = securityHash
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "CUSTOMER",
                targetUserId = user.id,
                title = "Payment Successful ✅",
                message = "₱$amountToPay payment received for Installment #${schedule.installmentNumber}. Reference: $ref.",
                type = "REPAYMENT"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                recipientRole = "ADMIN",
                title = "Loan Repayment Received",
                message = "${user.fullName} paid ₱$amountToPay for Loan #${schedule.loanId} via $channelName. Ref: $ref.",
                type = "REPAYMENT"
            )
        )

        messageDao.insertMessage(
            CommunicationGatewayEngine.createSmsAlert(
                userId = user.id,
                phone = user.phone,
                body = "CrediPulse PAYMENT: Received ₱$amountToPay for Installment #${schedule.installmentNumber}. Reference: $ref."
            )
        )
        messageDao.insertMessage(
            CommunicationGatewayEngine.createEmailReceipt(
                userId = user.id,
                email = user.email,
                subject = "Official Payment Receipt - $ref",
                body = "Dear ${user.fullName}, payment of ₱$amountToPay was processed for Installment #${schedule.installmentNumber} via $channelName. Ref: $ref."
            )
        )
        messageDao.insertMessage(
            CommunicationGatewayEngine.createAppPush(
                userId = user.id,
                title = "Payment Successful ✅",
                body = "₱$amountToPay paid for Installment #${schedule.installmentNumber}."
            )
        )

        return Result.success(transaction.copy(id = txId))
    }

    fun getSchedulesForLoan(loanId: Long): Flow<List<RepaymentScheduleEntity>> = repaymentDao.getSchedulesByLoanId(loanId)

    fun getSchedulesForUser(userId: Long): Flow<List<RepaymentScheduleEntity>> = repaymentDao.getSchedulesByUserId(userId)

    fun getAdminNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAdminNotifications()

    fun getCustomerNotifications(userId: Long): Flow<List<NotificationEntity>> = notificationDao.getCustomerNotifications(userId)

    suspend fun markNotificationRead(id: Long) = notificationDao.markAsRead(id)

    suspend fun markAllCustomerNotificationsRead(userId: Long) = notificationDao.markAllCustomerRead(userId)

    suspend fun markAllAdminNotificationsRead() = notificationDao.markAllAdminRead()

    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getUserTransactions(userId: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByUserId(userId)

    // Document Vault Operations - Per-Account Isolation
    fun getUserDocuments(userId: Long): Flow<List<UserDocumentEntity>> = userDocumentDao.getDocumentsByUserId(userId)

    fun getAllDocuments(): Flow<List<UserDocumentEntity>> = userDocumentDao.getAllDocuments()

    suspend fun uploadUserDocument(
        userId: Long,
        docType: String,
        title: String,
        fileName: String,
        fileSizeKb: Int,
        mimeType: String = "image/jpeg"
    ): Long {
        val hash = SecurityEncryptionEngine.sha256("DOC_${userId}_${fileName}_${System.currentTimeMillis()}")
        val doc = UserDocumentEntity(
            userId = userId,
            docType = docType,
            title = title,
            fileName = fileName,
            fileSizeKb = fileSizeKb,
            mimeType = mimeType,
            status = "IN_REVIEW",
            integrityHash = hash
        )
        val docId = userDocumentDao.insertDocument(doc)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actor = "USER_$userId",
                action = "DOCUMENT_UPLOADED",
                details = "Uploaded $docType file '$fileName' ($fileSizeKb KB) into encrypted account vault.",
                integrityHash = hash
            )
        )
        return docId
    }

    suspend fun updateDocumentStatus(docId: Long, status: String) {
        userDocumentDao.updateDocumentStatus(docId, status)
    }

    suspend fun deleteUserDocument(docId: Long, userId: Long) {
        userDocumentDao.deleteDocument(docId, userId)
    }

    // Communication Message Gateway Streams
    fun getUserMessages(userId: Long): Flow<List<CommunicationMessageEntity>> = messageDao.getMessagesByUserId(userId)

    fun getAllMessages(): Flow<List<CommunicationMessageEntity>> = messageDao.getAllMessages()

    suspend fun sendTestGatewayMessage(
        userId: Long,
        channel: String,
        recipient: String,
        subject: String,
        body: String
    ) {
        val msg = when (channel) {
            "SMS" -> CommunicationGatewayEngine.createSmsAlert(userId, recipient, body)
            "EMAIL" -> CommunicationGatewayEngine.createEmailReceipt(userId, recipient, subject, body)
            else -> CommunicationGatewayEngine.createAppPush(userId, subject, body)
        }
        messageDao.insertMessage(msg)
    }

    suspend fun simulateIncomingLoanApplication(): Result<Long> {
        val candidates = listOf(
            Triple("sophia_chen", "Sophia Chen", 65000.0),
            Triple("marcus_vance", "Marcus Vance", 42000.0),
            Triple("elena_rostova", "Elena Rostova", 88000.0),
            Triple("carlos_mendoza", "Carlos Mendoza", 35000.0)
        )
        val selected = candidates.random()
        var demoUser = userDao.getUserByUsername(selected.first)
        if (demoUser == null) {
            val newUser = UserEntity(
                username = selected.first,
                passwordHash = SecurityEncryptionEngine.hashPassword("demo123456"),
                fullName = selected.second,
                email = "${selected.first}@example.com",
                phone = "+63 917 555 ${(1000..9999).random()}",
                role = "CUSTOMER",
                kycStatus = if (listOf(true, false).random()) "VERIFIED" else "PENDING_VERIFICATION",
                idType = "National ID",
                idNumberEncrypted = SecurityEncryptionEngine.encryptField("ID-${System.currentTimeMillis().toString().takeLast(6)}"),
                employmentType = listOf("Employed", "Business Owner", "Self-Employed").random(),
                companyName = "Global Enterprises Inc.",
                monthlyIncome = selected.third,
                proofDocInfo = "Verified payslip / certificate of employment"
            )
            val newId = userDao.insertUser(newUser)
            demoUser = newUser.copy(id = newId)
        }

        val amounts = listOf(15000.0, 20000.0, 30000.0, 50000.0, 75000.0)
        val tenors = listOf(3, 6, 12)
        val types = listOf("Personal Loan", "Business Micro-Credit", "Emergency Advance", "Gadget Financing")

        return applyForLoan(
            user = demoUser,
            loanType = types.random(),
            principalAmount = amounts.random(),
            tenorMonths = tenors.random(),
            purpose = "Working capital and business expansion support"
        )
    }
}

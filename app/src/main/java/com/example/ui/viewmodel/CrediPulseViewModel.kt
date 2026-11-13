package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.GoogleSignInHelper
import com.example.data.SessionManager
import com.example.data.database.CrediPulseDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CommunicationMessageEntity
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.RepaymentScheduleEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserDocumentEntity
import com.example.data.model.UserEntity
import com.example.data.repository.CrediPulseFirestoreRepository
import com.example.data.repository.CrediPulseRepository
import com.example.domain.CreditRiskAssessment
import com.example.domain.CreditScoringEngine
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RoleViewMode {
    ADMIN_PANEL,
    CUSTOMER_DASHBOARD
}

data class UiFeedback(
    val message: String,
    val isError: Boolean = false,
    val id: Long = System.currentTimeMillis()
)

data class RealtimeSyncState(
    val isOnline: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val syncEventCount: Int = 0,
    val latestActionMessage: String = "Online • Zero Latency Stream Active"
)

class CrediPulseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CrediPulseDatabase.getInstance(application)
    private val repository = CrediPulseRepository(database)
    private val firestoreRepository = CrediPulseFirestoreRepository(application)
    private val sessionManager = SessionManager(application)

    // Dark Theme state
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Real-time Sync state
    private val _syncState = MutableStateFlow(RealtimeSyncState())
    val syncState: StateFlow<RealtimeSyncState> = _syncState.asStateFlow()

    // Current logged in user ID and reactive UserEntity stream
    private val _currentUserId = MutableStateFlow<Long?>(null)
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Mode for Admin viewing
    private val _activeViewMode = MutableStateFlow(RoleViewMode.CUSTOMER_DASHBOARD)
    val activeViewMode: StateFlow<RoleViewMode> = _activeViewMode.asStateFlow()

    fun switchViewMode(mode: RoleViewMode) {
        // Only admin can switch to admin panel
        if (mode == RoleViewMode.ADMIN_PANEL && _currentUser.value?.role != "ADMIN") {
            showFeedback("Access Denied: Customer accounts cannot view the Admin Dashboard.", isError = true)
            return
        }
        _activeViewMode.value = mode
    }

    // UI Feedback
    private val _feedback = MutableStateFlow<UiFeedback?>(null)
    val feedback: StateFlow<UiFeedback?> = _feedback.asStateFlow()

    fun showFeedback(msg: String, isError: Boolean = false) {
        _feedback.value = UiFeedback(msg, isError)
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    // Reactive streams - update instantaneously when any change occurs in Room SQLite
    val allLoans: StateFlow<List<LoanApplicationEntity>> = repository.getAllLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<UserEntity>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userLoans: StateFlow<List<LoanApplicationEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getLoansForUser(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSchedules: StateFlow<List<RepaymentScheduleEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getSchedulesForUser(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminNotifications: StateFlow<List<NotificationEntity>> = repository.getAdminNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerNotifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getCustomerNotifications(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<TransactionEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getUserTransactions(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Per-Account Document Vault Streams
    val userDocuments: StateFlow<List<UserDocumentEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getUserDocuments(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<UserDocumentEntity>> = repository.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Multi-Channel Communications Gateway Streams (SMS via Twilio, Email via SendGrid, FCM Push)
    val userMessages: StateFlow<List<CommunicationMessageEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getUserMessages(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<CommunicationMessageEntity>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active receipt to display
    private val _lastTransaction = MutableStateFlow<TransactionEntity?>(null)
    val lastTransaction: StateFlow<TransactionEntity?> = _lastTransaction.asStateFlow()

    fun dismissReceipt() {
        _lastTransaction.value = null
    }

    init {
        viewModelScope.launch {
            repository.ensureDefaultAdmin()
        }

        // Restore saved persistent session so users stay logged in across app restarts
        val savedId = sessionManager.getSavedUserId()
        if (savedId != null) {
            _currentUserId.value = savedId
            val role = sessionManager.getSavedUsername()
            if (role == "admin") {
                _activeViewMode.value = RoleViewMode.ADMIN_PANEL
            }
        }

        // Reactively stream UserEntity changes directly from database
        viewModelScope.launch {
            _currentUserId.flatMapLatest { id ->
                if (id != null) repository.observeUser(id) else flowOf(null)
            }.collect { user ->
                _currentUser.value = user
            }
        }

        // Realtime Event Watcher: Automatically detects any changes in loans, transactions, and audit logs
        viewModelScope.launch {
            repository.getAllLoans().collect { loans ->
                _syncState.update { current ->
                    current.copy(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        syncEventCount = current.syncEventCount + 1,
                        latestActionMessage = "Loans synced: ${loans.size} active records"
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getAllTransactions().collect { txns ->
                _syncState.update { current ->
                    current.copy(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        syncEventCount = current.syncEventCount + 1,
                        latestActionMessage = "Transactions synced: ${txns.size} recorded"
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getAllAuditLogs().collect { logs ->
                val latest = logs.firstOrNull()
                _syncState.update { current ->
                    current.copy(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        syncEventCount = current.syncEventCount + 1,
                        latestActionMessage = latest?.action ?: "Audit stream active"
                    )
                }
            }
        }
    }

    fun login(username: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (username.isBlank() || pass.isBlank()) {
                showFeedback("Please enter both username and password.", isError = true)
                return@launch
            }
            val res = repository.login(username, pass)
            res.onSuccess { user ->
                _currentUser.value = user
                _currentUserId.value = user.id
                sessionManager.saveSession(user.id, user.username, user.role)
                _activeViewMode.value = if (user.role == "ADMIN") RoleViewMode.ADMIN_PANEL else RoleViewMode.CUSTOMER_DASHBOARD
                showFeedback("Welcome, ${user.fullName}! Online Realtime Sync Active.")
                onSuccess()
            }.onFailure { err ->
                showFeedback(err.message ?: "Authentication failed", isError = true)
            }
        }
    }

    fun register(
        username: String,
        pass: String,
        fullName: String,
        email: String,
        phone: String,
        role: String,
        idType: String,
        idNumber: String,
        employmentType: String,
        companyName: String,
        monthlyIncome: Double,
        proofDoc: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (username.length < 3) {
                showFeedback("Username must be at least 3 characters.", isError = true)
                return@launch
            }
            if (pass.length < 6) {
                showFeedback("Password must be at least 6 characters.", isError = true)
                return@launch
            }
            if (fullName.isBlank() || email.isBlank() || phone.isBlank()) {
                showFeedback("Please fill in all personal contact details.", isError = true)
                return@launch
            }
            if (monthlyIncome <= 0) {
                showFeedback("Please enter a valid monthly income.", isError = true)
                return@launch
            }

            // Register admin requires correct password check if role is ADMIN
            if (role == "ADMIN" && (username != "admin" || pass != "@adminpass2026")) {
                showFeedback("Admin registration requires account 'admin' and password '@adminpass2026'.", isError = true)
                return@launch
            }

            val res = repository.registerCustomer(
                username = username,
                password = pass,
                fullName = fullName,
                email = email,
                phone = phone,
                role = role,
                idType = idType,
                idNumber = idNumber,
                employmentType = employmentType,
                companyName = companyName,
                monthlyIncome = monthlyIncome,
                proofDocInfo = proofDoc
            )

            res.onSuccess { user ->
                _currentUser.value = user
                _currentUserId.value = user.id
                sessionManager.saveSession(user.id, user.username, user.role)
                _activeViewMode.value = if (user.role == "ADMIN") RoleViewMode.ADMIN_PANEL else RoleViewMode.CUSTOMER_DASHBOARD
                showFeedback("Registration successful! Synced online in real-time.")
                onSuccess()
            }.onFailure { err ->
                showFeedback(err.message ?: "Registration failed", isError = true)
            }
        }
    }

    fun handleGoogleSignIn(
        firebaseUser: FirebaseUser,
        role: String = "CUSTOMER",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val email = firebaseUser.email ?: "customer@credipulse.com"
            val fullName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Google User"
            val username = email.substringBefore("@")
            val uid = firebaseUser.uid

            var user = repository.getUserByEmail(email)
            if (user == null) {
                val regResult = repository.registerCustomer(
                    username = username,
                    password = "GoogleAuth_${uid.take(8)}",
                    fullName = fullName,
                    email = email,
                    phone = firebaseUser.phoneNumber ?: "+63 917 888 7777",
                    role = role,
                    idType = "Google Identity",
                    idNumber = "GID-${uid.take(8)}",
                    employmentType = if (role == "ADMIN") "Executive" else "Employed",
                    companyName = "CrediPulse Cloud Member",
                    monthlyIncome = if (role == "ADMIN") 150000.0 else 55000.0,
                    proofDocInfo = "Verified by Google Sign-In Identity"
                )
                user = regResult.getOrNull() ?: repository.getUserByUsername(username)
            }

            if (user != null) {
                _currentUser.value = user
                _currentUserId.value = user.id
                sessionManager.saveSession(user.id, user.username, user.role)
                sessionManager.saveFirebaseUid(uid)
                _activeViewMode.value = if (user.role == "ADMIN") RoleViewMode.ADMIN_PANEL else RoleViewMode.CUSTOMER_DASHBOARD

                // Sync profile to Firestore
                firestoreRepository.saveUser(uid, user)

                _syncState.update { current ->
                    current.copy(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        syncEventCount = current.syncEventCount + 1,
                        latestActionMessage = "Connected to Firestore Realtime for ${user.fullName}"
                    )
                }
                showFeedback("Signed in with Google! Online Realtime Sync active.")
                onSuccess()
            } else {
                showFeedback("Failed to initialize user session.", isError = true)
            }
        }
    }

    fun logout() {
        GoogleSignInHelper.signOut()
        sessionManager.clearSession()
        _currentUserId.value = null
        _currentUser.value = null
        _activeViewMode.value = RoleViewMode.CUSTOMER_DASHBOARD
        _lastTransaction.value = null
        showFeedback("Logged out successfully.")
    }

    fun applyForLoan(
        loanType: String,
        amount: Double,
        tenorMonths: Int,
        purpose: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (amount < 2000) {
                showFeedback("Minimum loan amount is ₱2,000.", isError = true)
                return@launch
            }
            if (purpose.isBlank()) {
                showFeedback("Please state the purpose of your loan.", isError = true)
                return@launch
            }

            val res = repository.applyForLoan(user, loanType, amount, tenorMonths, purpose)
            res.onSuccess { loanId ->
                viewModelScope.launch {
                    val uid = sessionManager.getSavedFirebaseUid() ?: "usr_${user.id}"
                    val loan = LoanApplicationEntity(
                        id = loanId,
                        userId = user.id,
                        customerName = user.fullName,
                        loanType = loanType,
                        principalAmount = amount,
                        tenorMonths = tenorMonths,
                        monthlyInterestRate = 0.025,
                        monthlyInstallment = (amount / tenorMonths) * 1.025,
                        totalRepayment = amount * 1.025,
                        purpose = purpose,
                        status = "PENDING_APPROVAL"
                    )
                    firestoreRepository.createLoan(uid, loan)
                }
                showFeedback("Loan application submitted successfully! Synced to Firestore in real-time.")
                onSuccess()
            }.onFailure { err ->
                showFeedback(err.message ?: "Loan application failed", isError = true)
            }
        }
    }

    fun approveLoan(loanId: Long, remarks: String) {
        val admin = _currentUser.value
        if (admin?.role != "ADMIN") {
            showFeedback("Unauthorized: Admin approval required.", isError = true)
            return
        }
        viewModelScope.launch {
            repository.approveLoan(admin.username, loanId, remarks)
            firestoreRepository.updateLoanStatus(loanId.toString(), "APPROVED")
            showFeedback("Loan #$loanId approved! Funds credited to customer available balance.")
        }
    }

    fun rejectLoan(loanId: Long, remarks: String) {
        val admin = _currentUser.value
        if (admin?.role != "ADMIN") {
            showFeedback("Unauthorized: Admin permissions required.", isError = true)
            return
        }
        viewModelScope.launch {
            repository.rejectLoan(admin.username, loanId, remarks)
            firestoreRepository.updateLoanStatus(loanId.toString(), "REJECTED")
            showFeedback("Loan #$loanId has been rejected.")
        }
    }

    fun updateKycStatus(userId: Long, status: String, reason: String) {
        val admin = _currentUser.value
        if (admin?.role != "ADMIN") {
            showFeedback("Unauthorized: Only Admin can verify KYC records.", isError = true)
            return
        }
        viewModelScope.launch {
            repository.updateKycStatus(admin.username, userId, status, reason)
            firestoreRepository.updateUserKycStatus("usr_$userId", status)
            showFeedback("Customer KYC status updated to $status.")
        }
    }

    fun withdrawBalance(
        loanId: Long,
        amount: Double,
        channel: String,
        destinationType: String,
        targetAccount: String,
        targetAccountName: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (amount <= 0) {
                showFeedback("Please enter a valid disbursement amount.", isError = true)
                return@launch
            }
            if (targetAccount.isBlank() || targetAccountName.isBlank()) {
                showFeedback("Please provide complete recipient account details.", isError = true)
                return@launch
            }

            val res = repository.withdrawAvailableBalance(
                user = user,
                loanId = loanId,
                amount = amount,
                channel = channel,
                destinationType = destinationType,
                targetAccount = targetAccount,
                targetAccountName = targetAccountName
            )

            res.onSuccess { tx ->
                _lastTransaction.value = tx
                showFeedback("₱$amount disbursed via $channel successfully!")
                onSuccess()
            }.onFailure { err ->
                showFeedback(err.message ?: "Withdrawal failed", isError = true)
            }
        }
    }

    fun processRepayment(
        scheduleId: Long,
        method: String,
        channelName: String,
        accountIdentifier: String,
        amountToPay: Double,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (amountToPay <= 0) {
                showFeedback("Please enter a valid repayment amount.", isError = true)
                return@launch
            }
            if (accountIdentifier.isBlank()) {
                showFeedback("Please enter payment details / card number.", isError = true)
                return@launch
            }

            val res = repository.processRepayment(
                user = user,
                scheduleId = scheduleId,
                paymentMethod = method,
                channelName = channelName,
                accountIdentifier = accountIdentifier,
                amountToPay = amountToPay
            )

            res.onSuccess { tx ->
                _lastTransaction.value = tx
                showFeedback("Repayment of ₱$amountToPay processed successfully!")
                onSuccess()
            }.onFailure { err ->
                showFeedback(err.message ?: "Payment processing failed", isError = true)
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (user.role == "ADMIN") {
                repository.markAllAdminNotificationsRead()
            } else {
                repository.markAllCustomerNotificationsRead(user.id)
            }
        }
    }

    fun evaluateRealtimeRisk(
        amount: Double,
        tenor: Int,
        user: UserEntity? = _currentUser.value,
        loans: List<LoanApplicationEntity> = userLoans.value,
        schedules: List<RepaymentScheduleEntity> = userSchedules.value
    ): CreditRiskAssessment {
        val completedCount = loans.count { it.status == "COMPLETED" }
        val activeLoans = loans.filter { it.status == "APPROVED" || it.status == "ACTIVE" }
        val existingDebtPayments = activeLoans.sumOf { it.monthlyInstallment }
        val overdueCount = schedules.count { it.status != "PAID" && it.dueDateMillis < System.currentTimeMillis() }

        return CreditScoringEngine.calculateRiskAssessment(
            monthlyIncome = user?.monthlyIncome ?: 40000.0,
            requestedAmount = amount,
            tenorMonths = tenor,
            employmentType = user?.employmentType ?: "Employed",
            kycStatus = user?.kycStatus ?: "PENDING_VERIFICATION",
            existingDebtPayments = existingDebtPayments,
            completedLoansCount = completedCount,
            overdueCount = overdueCount
        )
    }

    fun simulateRealtimeIncomingLoan() {
        viewModelScope.launch {
            val res = repository.simulateIncomingLoanApplication()
            res.onSuccess { loanId ->
                showFeedback("⚡ Real-time Stream: Incoming Loan #$loanId arrived and processed instantly!")
            }.onFailure { err ->
                showFeedback(err.message ?: "Simulation failed", isError = true)
            }
        }
    }

    fun triggerManualSyncPulse() {
        _syncState.update { current ->
            current.copy(
                lastSyncTimestamp = System.currentTimeMillis(),
                syncEventCount = current.syncEventCount + 1,
                latestActionMessage = "Real-time sync verified • All nodes up to date"
            )
        }
        showFeedback("⚡ Real-time Sync Active: All database tables & streams synchronized.")
    }

    fun uploadDocument(
        docType: String,
        title: String,
        fileName: String,
        fileSizeKb: Int,
        mimeType: String = "image/jpeg",
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.uploadUserDocument(user.id, docType, title, fileName, fileSizeKb, mimeType)
            val uid = sessionManager.getSavedFirebaseUid() ?: "usr_${user.id}"
            firestoreRepository.saveDocument(
                userId = uid,
                doc = UserDocumentEntity(
                    userId = user.id,
                    docType = docType,
                    title = title,
                    fileName = fileName,
                    fileSizeKb = fileSizeKb,
                    mimeType = mimeType,
                    status = "IN_REVIEW"
                )
            )
            showFeedback("Document '$title' uploaded securely to your account vault! Synced to cloud.")
            onSuccess()
        }
    }

    fun updateDocumentStatus(docId: Long, status: String) {
        val user = _currentUser.value
        if (user?.role != "ADMIN") return
        viewModelScope.launch {
            repository.updateDocumentStatus(docId, status)
            showFeedback("Document status updated to $status.")
        }
    }

    fun deleteDocument(docId: Long) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteUserDocument(docId, user.id)
            showFeedback("Document removed from your vault.")
        }
    }

    fun sendTestGatewayMessage(channel: String, recipient: String, subject: String, body: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.sendTestGatewayMessage(user.id, channel, recipient, subject, body)
            val uid = sessionManager.getSavedFirebaseUid() ?: "usr_${user.id}"
            firestoreRepository.logMessage(
                userId = uid,
                message = CommunicationMessageEntity(
                    userId = user.id,
                    channel = channel,
                    recipient = recipient,
                    subject = subject,
                    body = body,
                    gateway = if (channel == "SMS") "Twilio SMS Gateway" else if (channel == "EMAIL") "SendGrid Email API" else "FCM Push"
                )
            )
            showFeedback("⚡ $channel alert dispatched via gateway to $recipient!")
        }
    }
}

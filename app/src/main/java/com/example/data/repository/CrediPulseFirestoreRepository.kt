package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.CommunicationMessageEntity
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.UserDocumentEntity
import com.example.data.model.UserEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepo"

class CrediPulseFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // ----------------- USERS -----------------

    suspend fun saveUser(userId: String, user: UserEntity): Result<Unit> {
        return runCatching {
            val kycToSave = when (user.kycStatus) {
                "VERIFIED" -> "VERIFIED"
                "REJECTED" -> "REJECTED"
                else -> "PENDING_VERIFICATION"
            }
            val data = hashMapOf<String, Any>(
                "username" to user.username,
                "fullName" to user.fullName,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role,
                "kycStatus" to kycToSave,
                "monthlyIncome" to user.monthlyIncome,
                "companyName" to user.companyName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId).set(data).await()
            Log.d(TAG, "Saved user profile for $userId")
        }
    }

    suspend fun updateUserKycStatus(userId: String, kycStatus: String): Result<Unit> {
        return runCatching {
            val statusToSave = when (kycStatus) {
                "VERIFIED" -> "VERIFIED"
                "REJECTED" -> "REJECTED"
                else -> "PENDING_VERIFICATION"
            }
            db.collection("users").document(userId)
                .update("kycStatus", statusToSave)
                .await()
        }
    }

    fun observeUser(userId: String): Flow<UserEntity?> {
        return db.collection("users").document(userId)
            .snapshots()
            .map { doc ->
                if (doc.exists()) {
                    mapDocToUser(doc)
                } else null
            }
            .catch { e ->
                Log.w(TAG, "Error observing user $userId: ${e.message}")
                emit(null)
            }
    }

    fun observeAllUsers(): Flow<List<UserEntity>> {
        return db.collection("users")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { mapDocToUser(it) }
            }
            .catch { e ->
                Log.w(TAG, "Error observing all users: ${e.message}")
                emit(emptyList())
            }
    }

    private fun mapDocToUser(doc: DocumentSnapshot): UserEntity {
        val income = (doc.get("monthlyIncome") as? Number)?.toDouble() ?: 45000.0
        val role = doc.getString("role") ?: "CUSTOMER"
        val kyc = doc.getString("kycStatus") ?: "PENDING_VERIFICATION"
        return UserEntity(
            id = (doc.id.hashCode().toLong() and 0x7FFFFFFF),
            username = doc.getString("username") ?: doc.id,
            passwordHash = "",
            fullName = doc.getString("fullName") ?: "Customer",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            role = role,
            kycStatus = if (kyc == "PENDING_VERIFICATION") "PENDING_VERIFICATION" else kyc,
            idType = "National ID",
            idNumberEncrypted = "",
            employmentType = if (role == "ADMIN") "Executive" else "Employed",
            companyName = doc.getString("companyName") ?: "Enterprise Co.",
            monthlyIncome = income,
            registeredAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
        )
    }

    // ----------------- LOANS -----------------

    suspend fun createLoan(userId: String, loan: LoanApplicationEntity): Result<String> {
        return runCatching {
            val loanDoc = db.collection("loans").document()
            val data = hashMapOf<String, Any>(
                "userId" to userId,
                "customerName" to loan.customerName,
                "loanType" to loan.loanType,
                "principalAmount" to loan.principalAmount,
                "tenorMonths" to loan.tenorMonths,
                "status" to "PENDING_APPROVAL",
                "availableBalance" to loan.principalAmount,
                "remainingBalance" to (loan.principalAmount * (1.0 + loan.monthlyInterestRate)),
                "createdAt" to FieldValue.serverTimestamp()
            )
            loanDoc.set(data).await()
            Log.d(TAG, "Created loan ${loanDoc.id} in Firestore")
            loanDoc.id
        }
    }

    suspend fun updateLoanStatus(loanId: String, status: String): Result<Unit> {
        return runCatching {
            val firestoreStatus = when (status) {
                "APPROVED" -> "APPROVED"
                "REJECTED" -> "REJECTED"
                "ACTIVE", "DISBURSED" -> "ACTIVE"
                "COMPLETED" -> "COMPLETED"
                else -> "PENDING_APPROVAL"
            }
            db.collection("loans").document(loanId)
                .update("status", firestoreStatus)
                .await()
            Log.d(TAG, "Updated loan $loanId status to $firestoreStatus")
        }
    }

    fun observeUserLoans(userId: String): Flow<List<LoanApplicationEntity>> {
        return db.collection("loans")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { mapDocToLoan(it) }
            }
            .catch { e ->
                Log.w(TAG, "Error observing user loans: ${e.message}")
                emit(emptyList())
            }
    }

    fun observeAllLoans(): Flow<List<LoanApplicationEntity>> {
        return db.collection("loans")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { mapDocToLoan(it) }
            }
            .catch { e ->
                Log.w(TAG, "Error observing all loans: ${e.message}")
                emit(emptyList())
            }
    }

    private fun mapDocToLoan(doc: DocumentSnapshot): LoanApplicationEntity {
        val amount = (doc.get("principalAmount") as? Number)?.toDouble() ?: 10000.0
        val tenor = (doc.get("tenorMonths") as? Number)?.toInt() ?: 12
        val statusRaw = doc.getString("status") ?: "PENDING_APPROVAL"
        val userIdStr = doc.getString("userId") ?: ""
        val numericUserId = (userIdStr.hashCode().toLong() and 0x7FFFFFFF)
        val numericId = (doc.id.hashCode().toLong() and 0x7FFFFFFF)
        val interestRate = 0.025

        return LoanApplicationEntity(
            id = numericId,
            userId = numericUserId,
            customerName = doc.getString("customerName") ?: "Customer",
            loanType = doc.getString("loanType") ?: "Personal Cash Loan",
            principalAmount = amount,
            tenorMonths = tenor,
            monthlyInterestRate = interestRate,
            monthlyInstallment = (amount / tenor) * 1.025,
            totalRepayment = amount * 1.025,
            purpose = doc.getString("loanType") ?: "Credit Advance",
            status = statusRaw,
            riskScore = 750,
            riskTier = "LOW",
            availableBalance = (doc.get("availableBalance") as? Number)?.toDouble() ?: amount,
            remainingBalance = (doc.get("remainingBalance") as? Number)?.toDouble() ?: (amount * 1.025),
            appliedAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
            approvedAt = if (statusRaw == "APPROVED" || statusRaw == "ACTIVE") System.currentTimeMillis() else null
        )
    }

    // ----------------- USER DOCUMENTS -----------------

    suspend fun saveDocument(userId: String, doc: UserDocumentEntity): Result<String> {
        return runCatching {
            val docRef = db.collection("user_documents").document()
            val data = hashMapOf<String, Any>(
                "userId" to userId,
                "docType" to doc.docType,
                "fileName" to doc.fileName,
                "fileSizeKb" to doc.fileSizeKb,
                "mimeType" to doc.mimeType,
                "status" to (if (doc.status == "VERIFIED") "VERIFIED" else if (doc.status == "REJECTED") "REJECTED" else "IN_REVIEW"),
                "integrityHash" to doc.integrityHash,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            docRef.id
        }
    }

    fun observeUserDocuments(userId: String): Flow<List<UserDocumentEntity>> {
        return db.collection("user_documents")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { doc ->
                    val status = doc.getString("status") ?: "IN_REVIEW"
                    val docType = doc.getString("docType") ?: "GOVERNMENT_ID"
                    val fileName = doc.getString("fileName") ?: "document.pdf"
                    UserDocumentEntity(
                        id = (doc.id.hashCode().toLong() and 0x7FFFFFFF),
                        userId = (userId.hashCode().toLong() and 0x7FFFFFFF),
                        docType = docType,
                        title = docType.replace('_', ' '),
                        fileName = fileName,
                        fileSizeKb = (doc.get("fileSizeKb") as? Number)?.toInt() ?: 128,
                        mimeType = doc.getString("mimeType") ?: "application/pdf",
                        status = status,
                        integrityHash = doc.getString("integrityHash") ?: "SHA256_SEC",
                        uploadedAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }
            }
            .catch { e ->
                Log.w(TAG, "Error observing user docs: ${e.message}")
                emit(emptyList())
            }
    }

    // ----------------- MESSAGES -----------------

    suspend fun logMessage(userId: String?, message: CommunicationMessageEntity): Result<String> {
        return runCatching {
            val msgRef = db.collection("messages").document()
            val channelType = when (message.channel) {
                "SMS" -> "SMS"
                "EMAIL" -> "EMAIL"
                else -> "APP_PUSH"
            }
            val gateway = when {
                message.gateway.contains("Twilio") -> "TWILIO_SMS"
                message.gateway.contains("SendGrid") -> "SENDGRID_EMAIL"
                else -> "FCM_PUSH"
            }
            val data = hashMapOf<String, Any>(
                "userId" to (userId ?: ""),
                "channelType" to channelType,
                "recipient" to message.recipient,
                "subject" to message.subject,
                "body" to message.body,
                "externalGateway" to gateway,
                "status" to (if (message.status == "DELIVERED") "DELIVERED" else "SENT"),
                "createdAt" to FieldValue.serverTimestamp()
            )
            msgRef.set(data).await()
            msgRef.id
        }
    }

    fun observeMessages(userId: String): Flow<List<CommunicationMessageEntity>> {
        return db.collection("messages")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { doc ->
                    val channelType = doc.getString("channelType") ?: "APP_PUSH"
                    val recipient = doc.getString("recipient") ?: ""
                    val subject = doc.getString("subject") ?: ""
                    val body = doc.getString("body") ?: ""
                    val gateway = doc.getString("externalGateway") ?: "FCM_PUSH"
                    val status = doc.getString("status") ?: "DELIVERED"
                    CommunicationMessageEntity(
                        id = (doc.id.hashCode().toLong() and 0x7FFFFFFF),
                        userId = (userId.hashCode().toLong() and 0x7FFFFFFF),
                        channel = channelType,
                        recipient = recipient,
                        subject = subject,
                        body = body,
                        gateway = gateway,
                        status = status,
                        sentAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }
            }
            .catch { e ->
                Log.w(TAG, "Error observing messages: ${e.message}")
                emit(emptyList())
            }
    }
}

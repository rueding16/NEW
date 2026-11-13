package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.UserDocumentEntity
import com.example.data.model.UserEntity
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CrediPulseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUser_validPayload_createsUserDocument() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CrediPulseFirestoreRepository(firestore)

        val user = UserEntity(
            id = 1L,
            username = "alice_test",
            passwordHash = "",
            fullName = "Alice Smith",
            email = ALICE_EMAIL,
            phone = "+63 917 111 2222",
            role = "CUSTOMER",
            kycStatus = "PENDING_VERIFICATION",
            idType = "National ID",
            idNumberEncrypted = "",
            employmentType = "Employed",
            companyName = "Acme Corp",
            monthlyIncome = 50000.0
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUser(aliceUid, user)
        }
        assertTrue(saveResult.isSuccess)
    }

    @Test
    fun createLoan_authenticatedOwner_createsLoanAndEmitsInFlow() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CrediPulseFirestoreRepository(firestore)

        val loan = LoanApplicationEntity(
            id = 101L,
            userId = 1L,
            customerName = "Alice Smith",
            loanType = "Personal Cash Loan",
            principalAmount = 15000.0,
            tenorMonths = 12,
            monthlyInterestRate = 0.025,
            monthlyInstallment = 1300.0,
            totalRepayment = 15500.0,
            purpose = "Business Expansion",
            status = "PENDING_APPROVAL"
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createLoan(aliceUid, loan)
        }
        assertTrue(createResult.isSuccess)

        val userLoans = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserLoans(aliceUid).first { list -> list.isNotEmpty() }
        }
        assertTrue(userLoans.isNotEmpty())
        assertEquals(15000.0, userLoans.first().principalAmount, 0.001)
    }

    @Test
    fun saveDocument_authenticatedOwner_savesKycDocument() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CrediPulseFirestoreRepository(firestore)

        val doc = UserDocumentEntity(
            id = 201L,
            userId = 1L,
            docType = "PASSPORT",
            title = "Passport",
            fileName = "passport_scan.pdf",
            fileSizeKb = 340,
            mimeType = "application/pdf",
            status = "IN_REVIEW",
            integrityHash = "HASH-12345"
        )

        val docResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveDocument(aliceUid, doc)
        }
        assertTrue(docResult.isSuccess)
    }

    @Test
    fun crossUser_cannotReadOtherUserProfileDirectly() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = CrediPulseFirestoreRepository(firestore)
        val user = UserEntity(
            id = 1L,
            username = "alice_secret",
            passwordHash = "",
            fullName = "Alice Secret",
            email = ALICE_EMAIL,
            phone = "+63 917 111 2222",
            role = "CUSTOMER",
            kycStatus = "PENDING_VERIFICATION",
            idType = "National ID",
            idNumberEncrypted = "",
            employmentType = "Employed",
            companyName = "Acme Corp",
            monthlyIncome = 50000.0
        )
        aliceRepo.saveUser(aliceUid, user).getOrThrow()

        // Bob signs in
        val bobUid = signInTestUser(BOB_EMAIL)
        assertTrue(bobUid != aliceUid)

        // Bob attempts to read Alice's user doc directly
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
            fail("Expected PERMISSION_DENIED")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }
}

package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LoanApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loan_applications ORDER BY appliedAt DESC")
    fun getAllLoans(): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE userId = :userId ORDER BY appliedAt DESC")
    fun getLoansByUserId(userId: Long): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE id = :loanId LIMIT 1")
    suspend fun getLoanById(loanId: Long): LoanApplicationEntity?

    @Query("SELECT * FROM loan_applications WHERE id = :loanId LIMIT 1")
    fun observeLoanById(loanId: Long): Flow<LoanApplicationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanApplicationEntity): Long

    @Update
    suspend fun updateLoan(loan: LoanApplicationEntity)

    @Query("UPDATE loan_applications SET status = :status, adminRemarks = :remarks, approvedAt = :approvedAt, availableBalance = :availableBalance, remainingBalance = :remainingBalance WHERE id = :loanId")
    suspend fun updateLoanStatus(
        loanId: Long,
        status: String,
        remarks: String,
        approvedAt: Long?,
        availableBalance: Double,
        remainingBalance: Double
    )

    @Query("UPDATE loan_applications SET availableBalance = availableBalance - :amount, disbursedTotal = disbursedTotal + :amount WHERE id = :loanId")
    suspend fun deductAvailableBalance(loanId: Long, amount: Double)

    @Query("UPDATE loan_applications SET remainingBalance = remainingBalance - :amount WHERE id = :loanId")
    suspend fun deductRemainingBalance(loanId: Long, amount: Double)

    @Query("SELECT SUM(availableBalance) FROM loan_applications WHERE userId = :userId AND status IN ('APPROVED', 'ACTIVE')")
    fun observeTotalAvailableBalance(userId: Long): Flow<Double?>
}

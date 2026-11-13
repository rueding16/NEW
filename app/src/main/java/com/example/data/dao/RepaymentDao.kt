package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RepaymentScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepaymentDao {
    @Query("SELECT * FROM repayment_schedules WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getSchedulesByLoanId(loanId: Long): Flow<List<RepaymentScheduleEntity>>

    @Query("SELECT * FROM repayment_schedules WHERE userId = :userId ORDER BY dueDateMillis ASC")
    fun getSchedulesByUserId(userId: Long): Flow<List<RepaymentScheduleEntity>>

    @Query("SELECT * FROM repayment_schedules WHERE userId = :userId AND status != 'PAID' ORDER BY dueDateMillis ASC")
    fun getPendingSchedulesByUserId(userId: Long): Flow<List<RepaymentScheduleEntity>>

    @Query("SELECT * FROM repayment_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): RepaymentScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<RepaymentScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: RepaymentScheduleEntity)

    @Query("UPDATE repayment_schedules SET status = 'PAID', paidAmount = :paidAmount, paidDateMillis = :paidDate, paymentMethod = :method, referenceNumber = :ref WHERE id = :id")
    suspend fun markAsPaid(
        id: Long,
        paidAmount: Double,
        paidDate: Long,
        method: String,
        ref: String
    )
}

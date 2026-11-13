package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CommunicationMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationMessageDao {
    @Query("SELECT * FROM communication_messages WHERE userId = :userId ORDER BY sentAt DESC")
    fun getMessagesByUserId(userId: Long): Flow<List<CommunicationMessageEntity>>

    @Query("SELECT * FROM communication_messages ORDER BY sentAt DESC")
    fun getAllMessages(): Flow<List<CommunicationMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CommunicationMessageEntity): Long
}

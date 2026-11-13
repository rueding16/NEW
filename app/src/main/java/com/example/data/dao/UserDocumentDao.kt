package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDocumentDao {
    @Query("SELECT * FROM user_documents WHERE userId = :userId ORDER BY uploadedAt DESC")
    fun getDocumentsByUserId(userId: Long): Flow<List<UserDocumentEntity>>

    @Query("SELECT * FROM user_documents ORDER BY uploadedAt DESC")
    fun getAllDocuments(): Flow<List<UserDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: UserDocumentEntity): Long

    @Update
    suspend fun updateDocument(document: UserDocumentEntity)

    @Query("UPDATE user_documents SET status = :status WHERE id = :id")
    suspend fun updateDocumentStatus(id: Long, status: String)

    @Query("DELETE FROM user_documents WHERE id = :id AND userId = :userId")
    suspend fun deleteDocument(id: Long, userId: Long)
}

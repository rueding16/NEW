package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE recipientRole = 'ADMIN' OR recipientRole = 'ALL' ORDER BY timestamp DESC")
    fun getAdminNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE (recipientRole = 'CUSTOMER' AND targetUserId = :userId) OR recipientRole = 'ALL' ORDER BY timestamp DESC")
    fun getCustomerNotifications(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetUserId = :userId OR recipientRole = 'ALL'")
    suspend fun markAllCustomerRead(userId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientRole = 'ADMIN'")
    suspend fun markAllAdminRead()
}

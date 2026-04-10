package com.alejandrosahonero.courthub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandrosahonero.courthub.data.model.local.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("""
        SELECT * FROM notifications 
        WHERE userId = :userId 
        ORDER BY createdAt DESC
    """)
    fun getNotificationsByUser(userId: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT COUNT(*) FROM notifications 
        WHERE userId = :userId AND isRead = 0
    """)
    fun getUnreadCount(userId: String): Flow<Int>

    @Query("""
        UPDATE notifications 
        SET isRead = 1 
        WHERE notificationIdFirebase = :notificationId
    """)
    suspend fun markAsRead(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}
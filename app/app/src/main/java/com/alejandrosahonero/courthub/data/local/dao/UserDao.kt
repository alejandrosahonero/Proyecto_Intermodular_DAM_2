package com.alejandrosahonero.courthub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandrosahonero.courthub.data.model.local.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE uidFirebase = :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): UserEntity?

    @Query("DELETE FROM users WHERE uidFirebase = :uid")
    suspend fun deleteUserByUid(uid: String)
}
package com.alejandrosahonero.courthub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandrosahonero.courthub.data.model.local.CourtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courts: List<CourtEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourt(court: CourtEntity)

    @Query("SELECT * FROM courts ORDER BY name ASC")
    fun getAllCourts(): Flow<List<CourtEntity>>

    @Query("SELECT * FROM courts WHERE courtIdFirebase = :courtId LIMIT 1")
    suspend fun getCourtById(courtId: String): CourtEntity?

    @Query("UPDATE courts SET isEnabled = :isEnabled WHERE courtIdFirebase = :courtId")
    suspend fun updateCourtEnabled(courtId: String, isEnabled: Boolean)

    @Query("DELETE FROM courts")
    suspend fun deleteAll()
}
package com.alejandrosahonero.courthub.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alejandrosahonero.courthub.data.local.dao.CourtDao
import com.alejandrosahonero.courthub.data.local.dao.NotificationDao
import com.alejandrosahonero.courthub.data.local.dao.ReservationDao
import com.alejandrosahonero.courthub.data.local.dao.UserDao
import com.alejandrosahonero.courthub.data.model.local.CourtEntity
import com.alejandrosahonero.courthub.data.model.local.NotificationEntity
import com.alejandrosahonero.courthub.data.model.local.ReservationEntity
import com.alejandrosahonero.courthub.data.model.local.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CourtEntity::class,
        ReservationEntity::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun courtDao(): CourtDao
    abstract fun reservationDao(): ReservationDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "courthub_database"
                )
                    .fallbackToDestructiveMigration() // durante desarrollo, ok
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
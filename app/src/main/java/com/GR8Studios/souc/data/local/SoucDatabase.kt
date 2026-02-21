package com.GR8Studios.souc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.GR8Studios.souc.data.ScheduledPost
import com.GR8Studios.souc.data.UserProfile

@Database(
    entities = [ScheduledPost::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(PostConverters::class)
abstract class SoucDatabase : RoomDatabase() {
    abstract fun scheduledPostDao(): ScheduledPostDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: SoucDatabase? = null

        fun getInstance(context: Context): SoucDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SoucDatabase::class.java,
                    "souc-db"
                ).fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}

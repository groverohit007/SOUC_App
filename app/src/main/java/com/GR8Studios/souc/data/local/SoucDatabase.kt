package com.GR8Studios.souc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.GR8Studios.souc.data.ScheduledPost

@Database(entities = [ScheduledPost::class], version = 1, exportSchema = false)
@TypeConverters(PostConverters::class)
abstract class SoucDatabase : RoomDatabase() {
    abstract fun scheduledPostDao(): ScheduledPostDao

    companion object {
        @Volatile
        private var INSTANCE: SoucDatabase? = null

        fun getInstance(context: Context): SoucDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SoucDatabase::class.java,
                    "souc-db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

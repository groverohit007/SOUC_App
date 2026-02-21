package com.GR8Studios.souc.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.GR8Studios.souc.data.ScheduledPost
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledPostDao {
    @Query("SELECT * FROM scheduled_posts ORDER BY scheduledEpochMillis ASC")
    fun observePosts(): Flow<List<ScheduledPost>>

    @Query("SELECT * FROM scheduled_posts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ScheduledPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: ScheduledPost)

    @Update
    suspend fun update(post: ScheduledPost)

    @Delete
    suspend fun delete(post: ScheduledPost)

    @Query("DELETE FROM scheduled_posts WHERE id = :id")
    suspend fun deleteById(id: String)
}

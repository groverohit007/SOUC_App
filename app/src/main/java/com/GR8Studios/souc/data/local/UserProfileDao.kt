package com.GR8Studios.souc.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.GR8Studios.souc.data.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getById(uid: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    fun observeById(uid: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles ORDER BY lastLoginAt DESC")
    fun observeAll(): Flow<List<UserProfile>>

    @Query("SELECT COUNT(*) FROM user_profiles")
    fun observeUserCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scheduled_posts")
    fun observeTotalPostCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scheduled_posts WHERE status = 'POSTED'")
    fun observePostedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scheduled_posts WHERE status = 'SCHEDULED'")
    fun observeScheduledCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scheduled_posts WHERE status = 'FAILED'")
    fun observeFailedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfile)

    @Query("UPDATE user_profiles SET subscriptionTier = :tier WHERE uid = :uid")
    suspend fun updateSubscription(uid: String, tier: String)

    @Query("UPDATE user_profiles SET postsUsedThisMonth = postsUsedThisMonth + 1 WHERE uid = :uid")
    suspend fun incrementPostCount(uid: String)

    @Query("UPDATE user_profiles SET postsUsedThisMonth = 0")
    suspend fun resetAllMonthlyPostCounts()

    @Query("UPDATE user_profiles SET monthlyPostLimit = :limit WHERE subscriptionTier = 'free'")
    suspend fun updateFreePostLimit(limit: Int)
}

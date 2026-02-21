package com.GR8Studios.souc.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.GR8Studios.souc.data.local.PostConverters

@Entity(tableName = "scheduled_posts")
@TypeConverters(PostConverters::class)
data class ScheduledPost(
    @PrimaryKey val id: String,
    val mediaUri: String,
    val mediaName: String,
    val mediaType: String,
    val platforms: List<String>,
    val captionMap: Map<String, String>,
    val scheduledEpochMillis: Long,
    val status: String,
    val lastError: String?,
    val createdAt: Long
)

object PostStatus {
    const val SCHEDULED = "SCHEDULED"
    const val UPLOADING = "UPLOADING"
    const val POSTED = "POSTED"
    const val FAILED = "FAILED"
}

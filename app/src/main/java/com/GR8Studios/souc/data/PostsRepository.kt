package com.GR8Studios.souc.data

import android.content.Context
import com.GR8Studios.souc.data.local.ScheduledPostDao
import kotlinx.coroutines.flow.Flow

class PostsRepository(
    private val context: Context,
    private val dao: ScheduledPostDao
) {
    val posts: Flow<List<ScheduledPost>> = dao.observePosts()

    suspend fun createPost(post: ScheduledPost) {
        dao.insert(post.copy(status = PostStatus.SCHEDULED, lastError = null))
        PostScheduler.schedulePost(context, post.copy(status = PostStatus.SCHEDULED, lastError = null))
    }

    suspend fun updatePost(post: ScheduledPost) {
        dao.update(post)
        if (post.status == PostStatus.SCHEDULED) {
            PostScheduler.schedulePost(context, post)
        }
    }

    suspend fun deletePost(postId: String) {
        dao.deleteById(postId)
        PostScheduler.cancelPost(context, postId)
    }

    suspend fun retryPost(postId: String) {
        val post = dao.getById(postId) ?: return
        val retryAt = System.currentTimeMillis() + 10_000
        val updated = post.copy(
            status = PostStatus.SCHEDULED,
            lastError = null,
            scheduledEpochMillis = retryAt
        )
        dao.update(updated)
        PostScheduler.schedulePost(context, updated, runAtMillis = retryAt)
    }

    suspend fun duplicatePost(postId: String) {
        val original = dao.getById(postId) ?: return
        val copy = original.copy(
            id = "post_${System.currentTimeMillis()}",
            scheduledEpochMillis = original.scheduledEpochMillis + 24 * 60 * 60 * 1000,
            status = PostStatus.SCHEDULED,
            lastError = null,
            createdAt = System.currentTimeMillis()
        )
        createPost(copy)
    }
}

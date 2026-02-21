package com.GR8Studios.souc.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.GR8Studios.souc.data.PostStatus
import com.GR8Studios.souc.data.local.SoucDatabase
import kotlinx.coroutines.delay
import kotlin.random.Random

class PostPublishWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val postId = inputData.getString(KEY_POST_ID) ?: return Result.failure()
        val dao = SoucDatabase.getInstance(applicationContext).scheduledPostDao()
        val post = dao.getById(postId) ?: return Result.success()

        dao.update(post.copy(status = PostStatus.UPLOADING, lastError = null))
        delay(Random.nextLong(2_000, 4_000))

        return if (Random.nextInt(100) < 80) {
            dao.update(post.copy(status = PostStatus.POSTED, lastError = null))
            Result.success()
        } else {
            dao.update(post.copy(status = PostStatus.FAILED, lastError = "Simulated upload failure"))
            Result.failure()
        }
    }

    companion object {
        const val KEY_POST_ID = "key_post_id"
    }
}

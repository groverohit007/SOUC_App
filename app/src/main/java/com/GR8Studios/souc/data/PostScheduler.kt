package com.GR8Studios.souc.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.GR8Studios.souc.worker.PostPublishWorker
import java.util.concurrent.TimeUnit

object PostScheduler {
    fun schedulePost(context: Context, post: ScheduledPost, runAtMillis: Long = post.scheduledEpochMillis) {
        val delay = (runAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<PostPublishWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(PostPublishWorker.KEY_POST_ID to post.id))
            .addTag("post_${post.id}")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("post_${post.id}", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelPost(context: Context, postId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("post_$postId")
    }
}

package com.GR8Studios.souc.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.GR8Studios.souc.data.PostStatus
import com.GR8Studios.souc.data.local.SoucDatabase
import com.GR8Studios.souc.network.ApiClient
import com.GR8Studios.souc.network.CreatePostRequest
import com.GR8Studios.souc.network.SignedUrlRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostPublishWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_POST_ID = "key_post_id"
        private const val TAG = "PostPublishWorker"
    }

    override suspend fun doWork(): Result {
        val postId = inputData.getString(KEY_POST_ID) ?: return Result.failure()
        val dao = SoucDatabase.getInstance(applicationContext).scheduledPostDao()
        val post = dao.getById(postId) ?: return Result.success()

        dao.update(post.copy(status = PostStatus.UPLOADING, lastError = null))

        return try {
            // Step 1: Get signed upload URL from backend
            val mimeType = if (post.mediaType == "video") "video/mp4" else "image/jpeg"
            val signedUrlResponse = ApiClient.apiService.createSignedUrl(
                SignedUrlRequest(fileName = post.mediaName, mimeType = mimeType)
            )

            // Step 2: Upload media file to signed URL
            val mediaFile = File(post.mediaUri)
            if (mediaFile.exists()) {
                val client = OkHttpClient()
                val requestBody = mediaFile.asRequestBody(mimeType.toMediaType())
                val uploadRequest = Request.Builder()
                    .url(signedUrlResponse.signedUrl)
                    .put(requestBody)
                    .addHeader("Content-Type", mimeType)
                    .build()

                val uploadResponse = client.newCall(uploadRequest).execute()
                if (!uploadResponse.isSuccessful) {
                    throw Exception("Upload failed: ${uploadResponse.code}")
                }
            }

            // Step 3: Create post record on backend
            val scheduleAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                .format(Date(post.scheduledEpochMillis))

            val caption = post.captionMap.values.firstOrNull() ?: ""

            ApiClient.apiService.createPost(
                CreatePostRequest(
                    uid = postId,
                    platforms = post.platforms,
                    caption = caption,
                    videoUrl = signedUrlResponse.storagePath,
                    scheduleAt = scheduleAt
                )
            )

            // Step 4: Mark as posted
            dao.update(post.copy(status = PostStatus.POSTED, lastError = null))
            Log.i(TAG, "Post $postId published successfully")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Post $postId failed: ${e.message}", e)
            dao.update(post.copy(status = PostStatus.FAILED, lastError = e.message ?: "Upload failed"))
            Result.failure()
        }
    }
}

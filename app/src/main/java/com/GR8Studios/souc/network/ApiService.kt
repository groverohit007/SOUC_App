package com.GR8Studios.souc.network

import retrofit2.http.Body
import retrofit2.http.POST

// 1. Data classes to define what we send and receive
data class SignedUrlRequest(val fileName: String, val mimeType: String)
data class SignedUrlResponse(val signedUrl: String, val storagePath: String)

data class CreatePostRequest(
    val uid: String,
    val platforms: List<String>,
    val caption: String,
    val videoUrl: String,
    val scheduleAt: String
)
data class CreatePostResponse(val message: String, val postId: String)

// 2. The actual API calls
interface ApiService {

    @POST("uploads/createSignedUrl")
    suspend fun createSignedUrl(@Body request: SignedUrlRequest): SignedUrlResponse

    @POST("posts/create")
    suspend fun createPost(@Body request: CreatePostRequest): CreatePostResponse
}
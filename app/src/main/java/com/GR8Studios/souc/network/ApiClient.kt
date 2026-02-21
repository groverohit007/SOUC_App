package com.GR8Studios.souc.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // ⚠️ REPLACE THIS with your actual Cloud Run URL!
    // Make sure it ends with a forward slash /
    private const val BASE_URL = "https://souc-backend-236478370091.europe-west2.run.app/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
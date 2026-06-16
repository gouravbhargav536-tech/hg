package com.example.data.remote

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
data class ResendEmailRequest(
    val from: String,
    val to: List<String>,
    val subject: String,
    val text: String
)

interface ResendApiService {
    @POST("v1/emails")
    suspend fun sendEmail(
        @Header("Authorization") apiKey: String,
        @Body request: ResendEmailRequest
    )
}

object ResendClient {
    private const val BASE_URL = "https://api.resend.com/"

    val service: ResendApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ResendApiService::class.java)
    }
}

package com.textify.app.data.remote.api

import com.textify.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TextifyApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/sync/push")
    suspend fun pushData(
        @Header("Authorization") token: String,
        @Body syncData: SyncPackage
    ): Response<SyncResponse>

    @GET("api/sync/pull")
    suspend fun pullData(
        @Header("Authorization") token: String,
        @Query("userId") userId: String, // ID para filtrar en la nube
        @Query("lastSync") lastSync: Long
    ): Response<SyncPackage>
}

data class LoginRequest(val correo: String, val contrasena: String)
data class RegisterRequest(val nombre: String, val correo: String, val contrasena: String)
data class AuthResponse(val token: String, val userId: String, val nombre: String)

data class SyncPackage(
    val phrases: List<PhraseDto> = emptyList(),
    val conversations: List<ConversationDto> = emptyList()
)

data class SyncResponse(val success: Boolean, val timestamp: Long)

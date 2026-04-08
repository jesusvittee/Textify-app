package com.textify.app.data.remote.api

import com.textify.app.data.remote.dto.MessageDto
import com.textify.app.data.remote.dto.UserDto
import retrofit2.http.*

interface TextifyApiService {
    @GET("messages/{conversationId}")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String
    ): List<MessageDto>

    @POST("messages")
    suspend fun sendMessage(@Body message: MessageDto): MessageDto

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): UserDto
}
package com.textify.app.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ElevenLabsApi {
    @POST("text-to-speech/{voiceId}")
    @Streaming
    suspend fun textToSpeech(
        @Path("voiceId") voiceId: String,
        @Header("xi-api-key") apiKey: String,
        @Body request: TtsRequest
    ): Response<ResponseBody>
}

data class TtsRequest(
    val text: String,
    val model_id: String = "eleven_multilingual_v2",
    val voice_settings: VoiceSettings = VoiceSettings()
)

data class VoiceSettings(
    val stability: Float = 0.5f,
    val similarity_boost: Float = 0.75f
)

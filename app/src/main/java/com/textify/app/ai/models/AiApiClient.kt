package com.textify.app.ai.models

import com.textify.app.data.remote.api.ElevenLabsApi
import com.textify.app.data.remote.api.TextifyApiService
import com.textify.app.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AiApiClient {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val elevenLabsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.ELEVENLABS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val textifyApi: TextifyApiService by lazy {
        retrofit.create(TextifyApiService::class.java)
    }

    val elevenLabsApi: ElevenLabsApi by lazy {
        elevenLabsRetrofit.create(ElevenLabsApi::class.java)
    }
}

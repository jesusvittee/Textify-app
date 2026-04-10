package com.textify.app.services.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import com.textify.app.ai.models.AiApiClient
import com.textify.app.data.remote.api.TtsRequest
import com.textify.app.ui.screens.profile.VoiceGender
import com.textify.app.utils.Constants
import com.textify.app.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "MX"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("es", "ES"))
            }
            isInitialized = true
        }
    }

    fun speak(text: String, voiceId: String, gender: VoiceGender) {
        stop()

        val isOnline = NetworkUtils.isOnline(context)
        val hasApiKey = Constants.ELEVENLABS_API_KEY != "TU_API_KEY_AQUI" && Constants.ELEVENLABS_API_KEY.isNotBlank()

        Log.d("TTS_DEBUG", "Solicitud -> Texto: $text | VoiceID: $voiceId | Género: $gender | Online: $isOnline")

        if (isOnline && voiceId.startsWith("ai_") && hasApiKey) {
            speakWithElevenLabs(text, voiceId, gender)
        } else {
            speakNative(text, voiceId, gender)
        }
    }

    private fun speakWithElevenLabs(text: String, voiceId: String, gender: VoiceGender) {
        val elevenVoiceId = when (voiceId) {
            "ai_female_1" -> Constants.VOICE_ID_SOFIA
            "ai_male_1" -> Constants.VOICE_ID_ALEJANDRO
            else -> if (gender == VoiceGender.FEMALE) Constants.VOICE_ID_SOFIA else Constants.VOICE_ID_ALEJANDRO
        }

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    AiApiClient.elevenLabsApi.textToSpeech(
                        voiceId = elevenVoiceId,
                        apiKey = Constants.ELEVENLABS_API_KEY,
                        request = TtsRequest(text = text)
                    )
                }

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        playAudioStream(body.byteStream())
                    } ?: run { speakNative(text, voiceId, gender) }
                } else {
                    speakNative(text, voiceId, gender)
                }
            } catch (e: Exception) {
                speakNative(text, voiceId, gender)
            }
        }
    }

    private fun playAudioStream(inputStream: java.io.InputStream) {
        try {
            val tempFile = File.createTempFile("tts_cache", ".mp3", context.cacheDir)
            tempFile.deleteOnExit()
            
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(tempFile.absolutePath)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { 
                    it.release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            Log.e("TTS_DEBUG", "Error ElevenLabs: ${e.message}")
        }
    }

    private fun speakNative(text: String, voiceId: String, gender: VoiceGender) {
        if (!isInitialized) return
        
        val voices = tts?.voices ?: emptySet()
        val esVoices = voices.filter { it.locale.language == "es" }
        
        // Filtro MUCHO más estricto para masculino/femenino
        val maleKeywords = listOf("male", "man", "hombre", "m-", "ana", "fba")
        val femaleKeywords = listOf("female", "woman", "mujer", "f-", "sfb", "dfb")

        val genderFiltered = esVoices.filter { voice ->
            val name = voice.name.lowercase()
            if (gender == VoiceGender.MALE) {
                maleKeywords.any { name.contains(it) } && !femaleKeywords.any { name.contains(it) && it != "ana" }
            } else {
                femaleKeywords.any { name.contains(it) }
            }
        }.sortedBy { it.name }

        Log.d("TTS_DEBUG", "Voces encontradas para $gender: ${genderFiltered.size}")
        genderFiltered.forEach { Log.d("TTS_DEBUG", "Voz disponible: ${it.name}") }

        val finalVoices = if (genderFiltered.isNotEmpty()) genderFiltered else esVoices
        
        val index = when {
            voiceId.contains("1") -> 0
            voiceId.contains("2") -> 1
            voiceId.contains("3") -> 2
            else -> 0
        }
        
        if (finalVoices.isNotEmpty()) {
            val selectedVoice = finalVoices[index % finalVoices.size]
            Log.d("TTS_DEBUG", "Seleccionada voz nativa: ${selectedVoice.name}")
            tts?.voice = selectedVoice
        }

        // Si es hombre y la voz seleccionada sigue sonando femenina, forzamos un pitch MUCHO más grave
        val basePitch = if (gender == VoiceGender.MALE) 0.7f else 1.0f
        val variation = when {
            voiceId.contains("2") -> 0.1f
            voiceId.contains("3") -> -0.1f
            else -> 0.0f
        }
        
        tts?.setPitch(basePitch + variation)
        tts?.setSpeechRate(if (gender == VoiceGender.MALE) 0.9f else 1.0f)
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "native_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
    }
}

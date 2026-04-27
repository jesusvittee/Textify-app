package com.textify.app.ai.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import com.textify.app.ai.models.AiApiClient
import com.textify.app.data.remote.api.TtsRequest
import com.textify.app.data.remote.api.VoiceSettings
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
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setLanguage(Locale("es", "MX"))
            isInitialized = true
        }
    }

    private fun showToast(message: String) {
        mainHandler.post { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    fun speak(text: String, voiceId: String, gender: VoiceGender) {
        stop()
        val isOnline = NetworkUtils.isOnline(context)
        val hasApiKey = Constants.ELEVENLABS_API_KEY.isNotBlank() && !Constants.ELEVENLABS_API_KEY.contains("AQUI")

        Log.d("ELEVEN_DEBUG", ">>> PROCESANDO: $voiceId | Online: $isOnline")

        if (isOnline && voiceId.length > 10 && hasApiKey && !voiceId.contains("default")) {
            speakWithElevenLabs(text, voiceId, gender)
        } else {
            if (voiceId.length > 10 && !isOnline) showToast("Sin conexión: Usando voz del sistema")
            speakNative(text, voiceId, gender)
        }
    }

    private fun speakWithElevenLabs(text: String, voiceId: String, gender: VoiceGender) {
        scope.launch {
            try {
                val request = TtsRequest(
                    text = text,
                    model_id = "eleven_multilingual_v2",
                    voice_settings = VoiceSettings(stability = 0.5f, similarity_boost = 0.75f)
                )

                val response = withContext(Dispatchers.IO) {
                    AiApiClient.elevenLabsApi.textToSpeech(
                        voiceId = voiceId,
                        apiKey = Constants.ELEVENLABS_API_KEY,
                        request = request
                    )
                }

                if (response.isSuccessful) {
                    Log.d("ELEVEN_DEBUG", "✅ Audio descargado")
                    response.body()?.let { playAudioStream(it.byteStream()) }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ELEVEN_DEBUG", "❌ ERROR ${response.code()}: $errorBody")
                    if (response.code() == 402) showToast("Voz premium: Cambiando a voz del sistema")
                    speakNative(text, voiceId, gender)
                }
            } catch (e: Exception) {
                Log.e("ELEVEN_DEBUG", "❌ Error conexión: ${e.message}")
                speakNative(text, voiceId, gender)
            }
        }
    }

    private fun playAudioStream(inputStream: java.io.InputStream) {
        try {
            val tempFile = File(context.cacheDir, "voice_preview.mp3")
            FileOutputStream(tempFile).use { it.write(inputStream.readBytes()) }

            mainHandler.post {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setAudioAttributes(AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA).build())
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener { it.release(); mediaPlayer = null }
                }
            }
        } catch (e: Exception) { Log.e("ELEVEN_DEBUG", "Error playback: ${e.message}") }
    }

    private fun speakNative(text: String, voiceId: String, gender: VoiceGender) {
        if (!isInitialized) return
        val pitch = if (gender == VoiceGender.MALE) 0.8f else 1.0f
        tts?.setPitch(pitch)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "v_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        mediaPlayer?.let { if (it.isPlaying) it.stop(); it.release() }
        mediaPlayer = null
    }

    fun shutdown() { stop(); tts?.shutdown() }
}

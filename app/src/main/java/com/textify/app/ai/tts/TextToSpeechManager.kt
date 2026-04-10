package com.textify.app.ai.tts

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
            Log.d("IA_TTS", "Motor TTS inicializado")
        }
    }

    fun speak(text: String, voiceId: String, gender: VoiceGender) {
        stop()
        val isOnline = NetworkUtils.isOnline(context)
        val hasElevenKey = Constants.ELEVENLABS_API_KEY != "TU_API_KEY_AQUI" && Constants.ELEVENLABS_API_KEY.isNotBlank()

        Log.d("IA_TTS", "Procesando voz: $voiceId | Género: $gender | Online: $isOnline")

        // Prioridad 1: ElevenLabs (IA Premium)
        if (isOnline && voiceId.startsWith("ai_") && hasElevenKey) {
            speakWithElevenLabs(text, voiceId, gender)
        } 
        // Prioridad 2: Voces de Red de Google (Alta calidad, IA nativa)
        else {
            speakNativeAdvanced(text, voiceId, gender)
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
                    } ?: run { speakNativeAdvanced(text, voiceId, gender) }
                } else {
                    speakNativeAdvanced(text, voiceId, gender)
                }
            } catch (e: Exception) {
                speakNativeAdvanced(text, voiceId, gender)
            }
        }
    }

    private fun playAudioStream(inputStream: java.io.InputStream) {
        try {
            val tempFile = File.createTempFile("ia_cache", ".mp3", context.cacheDir)
            tempFile.deleteOnExit()
            FileOutputStream(tempFile).use { inputStream.copyTo(it) }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build())
                setDataSource(tempFile.absolutePath)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { it.release(); mediaPlayer = null }
            }
        } catch (e: Exception) {
            Log.e("IA_TTS", "Error IA Player: ${e.message}")
        }
    }

    private fun speakNativeAdvanced(text: String, voiceId: String, gender: VoiceGender) {
        if (!isInitialized) return
        
        val allVoices = tts?.voices ?: emptySet()
        val esVoices = allVoices.filter { it.locale.language == "es" }

        // Mapeo de voces de Google IA (Neural2/High Quality)
        val targetVoiceName = if (gender == VoiceGender.MALE) {
            when {
                voiceId.contains("1") -> "es-mx-x-fba-network"
                voiceId.contains("2") -> "es-mx-x-jfc-network"
                else -> "es-mx-x-fba-local"
            }
        } else {
            when {
                voiceId.contains("1") -> "es-mx-x-dfb-network"
                voiceId.contains("2") -> "es-mx-x-jfd-network"
                else -> "es-mx-x-dfb-local"
            }
        }

        val selectedVoice = esVoices.find { it.name.contains(targetVoiceName, ignoreCase = true) }
            ?: esVoices.find { voice ->
                val name = voice.name.lowercase()
                if (gender == VoiceGender.MALE) name.contains("male") || name.contains("m-") || name.contains("fba") || name.contains("jfc")
                else name.contains("female") || name.contains("f-") || name.contains("dfb") || name.contains("jfd")
            } ?: esVoices.firstOrNull()

        selectedVoice?.let {
            Log.d("IA_TTS", "Usando voz IA nativa: ${it.name}")
            tts?.voice = it
        }

        // Si es Masculino y la voz sigue siendo genérica, bajamos el pitch SIN distorsionar, solo para dar profundidad
        if (gender == VoiceGender.MALE && (tts?.voice?.name?.contains("female") == true || tts?.voice == null)) {
            tts?.setPitch(0.8f)
        } else {
            tts?.setPitch(1.0f)
        }

        tts?.setSpeechRate(1.0f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "native_ia_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        mediaPlayer?.let { if (it.isPlaying) it.stop(); it.release() }
        mediaPlayer = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
    }
}

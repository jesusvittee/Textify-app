package com.textify.app.ai.stt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSpeechRecognizer
import android.util.Log
import com.textify.app.services.audio.OfflineAudioService
import com.textify.app.utils.NetworkUtils

class SpeechRecognizer(private val context: Context) {

    private var googleRecognizer: AndroidSpeechRecognizer? = null
    private val offlineService = OfflineAudioService(context)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isUsingOffline = false

    init {
        // Inicialización silenciosa del modelo
        offlineService.loadModel { success ->
            if (!success && NetworkUtils.isOnline(context)) {
                offlineService.downloadModel(
                    onProgress = { Log.d("STT", it) },
                    onComplete = { Log.d("STT", "Modelo descargado: $it") }
                )
            }
        }
    }

    fun startListening(
        onReady: () -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        mainHandler.post {
            // PRIMERO: Detenemos cualquier instancia previa
            stopListening()
            
            if (NetworkUtils.isOnline(context)) {
                isUsingOffline = false
                startGoogle(onReady, onResult, onError)
            } else if (offlineService.isModelReady()) {
                isUsingOffline = true
                onReady()
                offlineService.startListening(
                    onResult = { text ->
                        mainHandler.post { onResult(text) }
                    },
                    onError = { err ->
                        mainHandler.post { onError(err) }
                    }
                )
            } else {
                onError("Sin conexión y paquete de voz no listo.")
            }
        }
    }

    private fun startGoogle(onReady: () -> Unit, onResult: (String) -> Unit, onError: (String) -> Unit) {
        googleRecognizer = AndroidSpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // AÑADIDO: Forzar a que no se detenga tan rápido
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
        }

        googleRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = onReady()
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(AndroidSpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(AndroidSpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }
            
            override fun onError(error: Int) {
                Log.e("STT", "Error Google: $error")
                // Ignorar el error 7 (No match) mientras se está grabando, para que no corte
                if (error == AndroidSpeechRecognizer.ERROR_NO_MATCH) return
                
                if (error == AndroidSpeechRecognizer.ERROR_NETWORK || error == AndroidSpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
                    if (offlineService.isModelReady()) {
                        isUsingOffline = true
                        startListening(onReady, onResult, onError)
                    } else onError("Error de red.")
                } else {
                    onError("Error de voz: $error")
                }
            }
            
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        googleRecognizer?.startListening(intent)
    }

    fun stopListening() {
        mainHandler.post {
            if (isUsingOffline) {
                offlineService.stop()
            }
            stopGoogle()
        }
    }

    private fun stopGoogle() {
        googleRecognizer?.stopListening()
        googleRecognizer?.cancel()
        googleRecognizer?.destroy()
        googleRecognizer = null
    }
}

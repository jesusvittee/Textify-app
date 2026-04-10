package com.textify.app.ai.stt

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException

class VoskSpeechRecognizer(private val context: Context) {

    private var model: Model? = null
    private var speechService: SpeechService? = null

    fun initModel(onComplete: (Boolean) -> Unit) {
        if (model != null) {
            onComplete(true)
            return
        }

        // Buscamos el modelo en la carpeta assets/model-es
        StorageService.unpack(context, "model-es", "model",
            { model: Model? ->
                this.model = model
                onComplete(model != null)
            },
            { exception: IOException ->
                Log.e("Vosk", "Error al cargar modelo offline: ${exception.message}")
                onComplete(false)
            }
        )
    }

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (model == null) {
            initModel { success ->
                if (success) startListening(onResult, onError)
                else onError("No se pudo cargar el modelo offline")
            }
            return
        }

        try {
            val rec = Recognizer(model, 16000.0f)
            speechService = SpeechService(rec, 16000.0f)
            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {
                    // Feedback visual opcional
                }

                override fun onResult(hypothesis: String?) {
                    val text = extractText(hypothesis)
                    if (text.isNotBlank()) onResult(text)
                }

                override fun onFinalResult(hypothesis: String?) {
                    val text = extractText(hypothesis)
                    if (text.isNotBlank()) onResult(text)
                }

                override fun onError(exception: Exception?) {
                    onError(exception?.message ?: "Error desconocido en Vosk")
                }

                override fun onTimeout() {
                    stopListening()
                }
            })
        } catch (e: Exception) {
            onError("Error al iniciar Vosk: ${e.message}")
        }
    }

    private fun extractText(hypothesis: String?): String {
        if (hypothesis == null) return ""
        // El resultado de Vosk es un JSON: {"text" : "hola mundo"}
        return hypothesis.substringAfter("\"text\" : \"").substringBefore("\"")
    }

    fun stopListening() {
        speechService?.stop()
        speechService?.cancel()
        speechService = null
    }
}

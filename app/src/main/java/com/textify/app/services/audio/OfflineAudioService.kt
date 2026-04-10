package com.textify.app.services.audio

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

class OfflineAudioService(private val context: Context) {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private val modelDir = File(context.filesDir, "vosk-model-es")

    fun isModelReady(): Boolean = modelDir.exists() && modelDir.listFiles()?.isNotEmpty() == true

    fun loadModel(onResult: (Boolean) -> Unit) {
        if (model != null) { onResult(true); return }
        if (isModelReady()) {
            try {
                val actualPath = getActualModelPath(modelDir)
                model = Model(actualPath)
                onResult(true)
            } catch (e: Exception) {
                modelDir.deleteRecursively()
                onResult(false)
            }
        } else { onResult(false) }
    }

    private fun getActualModelPath(root: File): String {
        val files = root.listFiles()
        if (files?.size == 1 && files[0].isDirectory) return files[0].absolutePath
        return root.absolutePath
    }

    fun downloadModel(onProgress: (String) -> Unit, onComplete: (Boolean) -> Unit) {
        Thread {
            try {
                if (modelDir.exists()) modelDir.deleteRecursively()
                modelDir.mkdirs()
                onProgress("Descargando paquete de voz...")
                val url = URL("https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip")
                val connection = url.openConnection()
                ZipInputStream(connection.getInputStream()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val file = File(modelDir, entry.name)
                        if (entry.isDirectory) file.mkdirs()
                        else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { zip.copyTo(it) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
                loadModel { onComplete(it) }
            } catch (e: Exception) {
                modelDir.deleteRecursively()
                onComplete(false)
            }
        }.start()
    }

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (model == null) {
            loadModel { if (it) startListening(onResult, onError) else onError("Error de carga") }
            return
        }

        stop() // Limpiar cualquier servicio previo

        try {
            // Creamos el reconocedor con una frecuencia de 16kHz
            val rec = Recognizer(model, 16000.0f)
            speechService = SpeechService(rec, 16000.0f)
            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {
                    // Importante: Capturar el texto parcial para que sea fluido
                    val text = parseVoskJson(hypothesis, "partial")
                    if (text.isNotBlank()) onResult(text)
                }

                override fun onResult(hypothesis: String?) {
                    val text = parseVoskJson(hypothesis, "text")
                    if (text.isNotBlank()) onResult(text)
                }

                override fun onFinalResult(hypothesis: String?) {
                    val text = parseVoskJson(hypothesis, "text")
                    if (text.isNotBlank()) onResult(text)
                }

                override fun onError(exception: Exception?) { onError(exception?.message ?: "Error offline") }
                override fun onTimeout() { stop() }
            })
        } catch (e: Exception) {
            onError("Error de inicio: ${e.message}")
        }
    }

    private fun parseVoskJson(json: String?, key: String): String {
        if (json == null) return ""
        // Extrae el valor de "text" o "partial" del JSON de Vosk
        return json.substringAfter("\"$key\" : \"").substringBefore("\"")
            .replace("\\n", " ").trim()
    }

    fun stop() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
    }
}

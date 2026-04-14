package com.textify.app.ui.screens.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.textify.app.services.audio.OfflineAudioService
import com.textify.app.ai.tts.TextToSpeechManager
import com.textify.app.data.local.database.AppDatabase
import com.textify.app.data.local.entity.*
import com.textify.app.data.remote.api.TextifyApiService
import com.textify.app.data.remote.api.SyncPackage
import com.textify.app.data.remote.dto.*
import com.textify.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

enum class UserType { DEAF, HEARING }
enum class FontSize { SMALL, MEDIUM, LARGE }
enum class VoiceGender { FEMALE, MALE }
enum class TtsSpeed { SLOW, NORMAL, FAST }

data class VoiceOption(
    val id: String,
    val name: String,
    val description: String,
    val initial: String,
    val isOnline: Boolean
)

data class ProfileUiState(
    val userId: String = "",
    val name: String = "Nicolás Vite",
    val email: String = "nicolas@example.com",
    val userType: UserType = UserType.DEAF,
    val isDarkMode: Boolean = false,
    val highContrast: Boolean = true,
    val fontSize: FontSize = FontSize.MEDIUM,
    val fontScale: Float = 1.0f,
    val voiceGender: VoiceGender = VoiceGender.MALE,
    val selectedVoiceId: String = "default_offline",
    val ttsSpeed: TtsSpeed = TtsSpeed.NORMAL,
    val hapticAlerts: Boolean = true,
    val localHistory: Boolean = true,
    val autoCleanupDays: Int = 30,
    val appLanguage: String = "Español (México)",
    
    val femaleVoices: List<VoiceOption> = listOf(
        VoiceOption("default_offline", "Sistema (Offline)", "Voz predeterminada", "S", false),
        VoiceOption("ai_female_1", "Sofía (IA)", "Voz suave y natural", "S", true),
        VoiceOption("ai_female_2", "Valentina (IA)", "Voz clara y profesional", "V", true),
        VoiceOption("ai_female_3", "Camila (IA)", "Voz amigable", "C", true)
    ),
    val maleVoices: List<VoiceOption> = listOf(
        VoiceOption("default_offline", "Sistema (Offline)", "Voz predeterminada", "S", false),
        VoiceOption("ai_male_1", "Alejandro (IA)", "Voz grave y firme", "A", true),
        VoiceOption("ai_male_2", "Diego (IA)", "Voz clara y profesional", "D", true),
        VoiceOption("ai_male_3", "Mateo (IA)", "Voz amigable", "M", true)
    ),
    
    val isOfflinePackageInstalled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: String = "",
    
    // Sync States
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0,
    val syncStatus: String = "No sincronizado"
)

class ProfileViewModel(
    application: Application,
    private val ttsManager: TextToSpeechManager
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState
    
    private val db = (application as com.textify.app.TextifyApp).database
    private val offlineAudioService = OfflineAudioService(application)

    private val api: TextifyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TextifyApiService::class.java)
    }

    init {
        _uiState.value = _uiState.value.copy(
            isOfflinePackageInstalled = offlineAudioService.isModelReady()
        )
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            db.usuarioDao().getUsuario().collect { user ->
                user?.let {
                    _uiState.value = _uiState.value.copy(
                        userId = it.id,
                        name = it.nombre,
                        email = it.correo
                    )
                }
            }
        }
    }

    fun performSync(pushLocal: Boolean) {
        if (_uiState.value.isSyncing) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncStatus = "Iniciando...")
            try {
                if (pushLocal) {
                    val unsyncedPhrases = db.phraseDao().getUnsyncedPhrases()
                    val unsyncedConvs = db.conversationDao().getUnsyncedConversations()
                    
                    if (unsyncedPhrases.isEmpty() && unsyncedConvs.isEmpty()) {
                        _uiState.value = _uiState.value.copy(syncStatus = "Todo está al día")
                        return@launch
                    }

                    val syncPackage = SyncPackage(
                        phrases = unsyncedPhrases.map { 
                            PhraseDto(it.id, it.usuarioId, it.text, it.categoria, it.isPinned, it.updatedAt) 
                        },
                        conversations = unsyncedConvs.map { 
                            ConversationDto(it.id, it.usuarioId, it.participantName, it.lastMessage, it.lastMessageTime, it.estado, it.isPinned, it.updatedAt) 
                        }
                    )
                    
                    val response = api.pushData("Bearer token", syncPackage)
                    if (response.isSuccessful) {
                        unsyncedPhrases.forEach { db.phraseDao().insertPhrase(it.copy(isSynced = true)) }
                        unsyncedConvs.forEach { db.conversationDao().insertConversation(it.copy(isSynced = true)) } // CORRECCIÓN: insertConversation en lugar de update
                        _uiState.value = _uiState.value.copy(syncStatus = "¡Subida exitosa!")
                    } else {
                        _uiState.value = _uiState.value.copy(syncStatus = "Error: ${response.code()}")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(syncStatus = "Bajando datos...")
                    // Enviamos 0 para forzar la descarga de todo lo que el servidor tiene
                    val response = api.pullData("Bearer token", 0) 
                    if (response.isSuccessful) {
                        val data = response.body()
                        Log.d("Sync", "Recibido: ${data?.phrases?.size} frases y ${data?.conversations?.size} chats")
                        
                        data?.phrases?.forEach { dto ->
                            db.phraseDao().insertPhrase(dto.toEntity())
                        }
                        data?.conversations?.forEach { dto ->
                            db.conversationDao().insertConversation(dto.toEntity()) // CORRECCIÓN: Usar insert para restaurar datos borrados
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            syncStatus = "¡Descarga completa!",
                            lastSyncTime = System.currentTimeMillis()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(syncStatus = "Error al bajar")
                    }
                }
            } catch (e: Exception) {
                Log.e("Sync", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(syncStatus = "Sin conexión")
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }

    fun setVoiceGender(gender: VoiceGender) {
        _uiState.value = _uiState.value.copy(voiceGender = gender, selectedVoiceId = "default_offline")
        saveConfig()
    }

    fun setSelectedVoice(voiceId: String) {
        _uiState.value = _uiState.value.copy(selectedVoiceId = voiceId)
        ttsManager.speak("Hola, esta es mi nueva voz.", voiceId, _uiState.value.voiceGender)
        saveConfig()
    }

    private fun saveConfig() {
        viewModelScope.launch {
            val config = ConfiguracionEntity(
                usuarioId = _uiState.value.userId,
                isDarkMode = _uiState.value.isDarkMode,
                fontScale = _uiState.value.fontScale,
                voiceGender = _uiState.value.voiceGender.name,
                selectedVoiceId = _uiState.value.selectedVoiceId,
                localHistoryEnabled = _uiState.value.localHistory
            )
            db.configuracionDao().insertConfiguracion(config)
        }
    }

    fun toggleDarkMode(enabled: Boolean) { 
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
        saveConfig()
    }
    
    fun setFontScale(scale: Float) { 
        _uiState.value = _uiState.value.copy(fontScale = scale)
        saveConfig()
    }

    fun toggleLocalHistory(enabled: Boolean) { 
        _uiState.value = _uiState.value.copy(localHistory = enabled)
        saveConfig()
    }

    fun downloadOfflinePackage() {
        _uiState.value = _uiState.value.copy(isDownloading = true)
        offlineAudioService.downloadModel(
            onProgress = { progress ->
                _uiState.value = _uiState.value.copy(downloadProgress = progress)
            },
            onComplete = { success ->
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    isOfflinePackageInstalled = success
                )
            }
        )
    }

    fun setTtsSpeed(speed: TtsSpeed) {
        _uiState.value = _uiState.value.copy(ttsSpeed = speed)
        saveConfig()
    }

    fun toggleHapticAlerts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hapticAlerts = enabled)
        saveConfig()
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            onComplete()
        }
    }

    class Factory(
        private val application: Application,
        private val ttsManager: TextToSpeechManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(application, ttsManager) as T
        }
    }
}

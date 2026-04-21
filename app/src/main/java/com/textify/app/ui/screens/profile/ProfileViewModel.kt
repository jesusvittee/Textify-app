package com.textify.app.ui.screens.profile

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.textify.app.services.audio.OfflineAudioService
import com.textify.app.ai.tts.TextToSpeechManager
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

enum class UserType { DEAF, HEARING }
enum class FontSize { SMALL, MEDIUM, LARGE }
enum class VoiceGender { FEMALE, MALE }
enum class TtsSpeed { SLOW, NORMAL, FAST }

data class VoiceOption(val id: String, val name: String, val description: String, val initial: String, val isOnline: Boolean)

data class ProfileUiState(
    val userId: String = "",
    val name: String = "Cargando...",
    val email: String = "",
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
        VoiceOption("default_offline", "Sistema (Offline)", "Predeterminada", "S", false),
        VoiceOption("ai_female_1", "Sofía (IA)", "Natural", "S", true)
    ),
    val maleVoices: List<VoiceOption> = listOf(
        VoiceOption("default_offline", "Sistema (Offline)", "Predeterminada", "S", false),
        VoiceOption("ai_male_1", "Alejandro (IA)", "Voz Grave", "A", true)
    ),
    val isOfflinePackageInstalled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: String = "",
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0,
    val syncStatus: String = "Listo"
)

class ProfileViewModel(
    application: Application,
    private val ttsManager: TextToSpeechManager
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState
    
    private val db = (application as com.textify.app.TextifyApp).database
    private val offlineAudioService = OfflineAudioService(application)
    private val prefs = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private val api: TextifyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TextifyApiService::class.java)
    }

    init { loadUserData() }

    private fun loadUserData() {
        viewModelScope.launch {
            db.usuarioDao().getUsuario().collect { user ->
                user?.let { _uiState.value = _uiState.value.copy(userId = it.id, name = it.nombre, email = it.correo) }
            }
        }
    }

    fun performSync(pushLocal: Boolean) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            val token = prefs.getString(Constants.KEY_USER_TOKEN, "") ?: ""

            if (userId.isEmpty()) {
                _uiState.value = _uiState.value.copy(syncStatus = "Error: Inicia sesión")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSyncing = true, syncStatus = "Sincronizando...")
            
            try {
                if (pushLocal) {
                    // PASO 1: Vaciado previo en la nube
                    _uiState.value = _uiState.value.copy(syncStatus = "Vaciando nube...")
                    try {
                        api.clearData("Bearer $token", userId)
                    } catch (e: Exception) {
                        Log.w("Sync", "Aviso al limpiar: ${e.message}")
                    }

                    // PASO 2: Preparar los datos locales
                    _uiState.value = _uiState.value.copy(syncStatus = "Preparando datos...")
                    val phrases = db.phraseDao().getPhrases().filter { it.usuarioId == userId }
                    val conversations = db.conversationDao().getConversationsByUsuarioSync(userId)
                    val conversationIds = conversations.map { it.id }.toSet()
                    val messages = db.messageDao().getAllMessages().filter { conversationIds.contains(it.conversationId) }

                    val syncPackage = SyncPackage(
                        userId = userId,
                        phrases = phrases.map { PhraseDto(it.id, userId, it.text, it.categoria, it.isPinned, it.updatedAt) },
                        conversations = conversations.map { ConversationDto(it.id, userId, it.participantName, it.lastMessage, it.lastMessageTime, it.estado, it.isPinned, it.updatedAt) },
                        messages = messages.map { MessageDto(it.id, it.conversationId, it.text, it.isOwn, it.timestamp, it.updatedAt) }
                    )

                    // PASO 3: Subir espejo
                    _uiState.value = _uiState.value.copy(syncStatus = "Subiendo espejo...")
                    val response = api.pushData("Bearer $token", syncPackage)
                    
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(syncStatus = "Nube actualizada ✅")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Sync", "Error en PUSH: $errorBody")
                        _uiState.value = _uiState.value.copy(syncStatus = "Error: ${response.code()}")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(syncStatus = "Bajando de la nube...")
                    val response = api.pullData("Bearer $token", userId, 0)
                    if (response.isSuccessful) {
                        val data = response.body()
                        
                        db.phraseDao().deleteAllPhrasesByUsuario(userId)
                        db.conversationDao().deleteAllConversationsByUsuario(userId)
                        db.messageDao().deleteAllMessagesByUsuario(userId)

                        data?.phrases?.forEach { db.phraseDao().insertPhrase(it.toEntity().copy(usuarioId = userId)) }
                        data?.conversations?.forEach { db.conversationDao().insertConversation(it.toEntity().copy(usuarioId = userId)) }
                        data?.messages?.forEach { db.messageDao().insertMessage(it.toEntity()) }

                        _uiState.value = _uiState.value.copy(syncStatus = "App restaurada ✅")
                    } else {
                        _uiState.value = _uiState.value.copy(syncStatus = "Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("Sync", "Excepción", e)
                _uiState.value = _uiState.value.copy(syncStatus = "Error de conexión")
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) { _uiState.value = _uiState.value.copy(isDarkMode = enabled) }
    fun setFontScale(scale: Float) { _uiState.value = _uiState.value.copy(fontScale = scale) }
    fun setVoiceGender(gender: VoiceGender) { _uiState.value = _uiState.value.copy(voiceGender = gender) }
    fun setSelectedVoice(id: String) { _uiState.value = _uiState.value.copy(selectedVoiceId = id) }
    fun setTtsSpeed(speed: TtsSpeed) { }
    fun toggleHapticAlerts(enabled: Boolean) { _uiState.value = _uiState.value.copy(hapticAlerts = enabled) }
    fun toggleLocalHistory(enabled: Boolean) { _uiState.value = _uiState.value.copy(localHistory = enabled) }
    fun downloadOfflinePackage() { }
    fun logout(onComplete: () -> Unit) { onComplete() }

    class Factory(
        private val application: Application,
        private val ttsManager: TextToSpeechManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(application, ttsManager) as T
        }
    }
}

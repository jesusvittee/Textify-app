package com.textify.app.ui.screens.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.textify.app.ai.tts.TextToSpeechManager
import com.textify.app.data.local.entity.*
import com.textify.app.data.remote.api.TextifyApiService
import com.textify.app.data.remote.api.SyncPackage
import com.textify.app.data.remote.dto.*
import com.textify.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

enum class UserType { DEAF, HEARING }
enum class FontSize { SMALL, MEDIUM, LARGE }
enum class VoiceGender { FEMALE, MALE }
enum class TtsSpeed { SLOW, NORMAL, FAST }

data class VoiceOption(val id: String, val name: String, val description: String, val initial: String, val isOnline: Boolean)

data class ProfileUiState(
    val userId: String = "",
    val name: String = "Usuario",
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
    val appLanguage: String = "Español",
    val femaleVoices: List<VoiceOption> = listOf(
        VoiceOption("default_offline", "Sistema", "Voz estándar", "S", false),
        VoiceOption("ai_female_1", "Sofía (IA)", "Voz natural", "S", true)
    ),
    val maleVoices: List<VoiceOption> = listOf(
        VoiceOption("default_offline", "Sistema", "Voz estándar", "S", false),
        VoiceOption("ai_male_1", "Alejandro (IA)", "Voz natural", "A", true)
    ),
    val isOfflinePackageInstalled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: String = "",
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0,
    val syncStatus: String = "Listo"
)

class ProfileViewModel(application: Application, private val ttsManager: TextToSpeechManager) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState
    private val db = (application as com.textify.app.TextifyApp).database

    private val api: TextifyApiService by lazy {
        Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(TextifyApiService::class.java)
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
            val user = db.usuarioDao().getUsuario().first()
            val userId = user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.value = _uiState.value.copy(syncStatus = "Error: Inicia sesión")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSyncing = true, syncStatus = "Sincronizando...")
            try {
                if (pushLocal) {
                    // CORRECCIÓN CLAVE: Obtenemos TODO y nos aseguramos de que el userId sea el correcto
                    val phrases = db.phraseDao().getPhrases().map { it.copy(usuarioId = userId) }
                    val conversations = db.conversationDao().getUnsyncedConversations().map { it.copy(usuarioId = userId) }

                    val syncPackage = SyncPackage(
                        phrases = phrases.map { PhraseDto(it.id, userId, it.text, it.categoria, it.isPinned, it.updatedAt) },
                        conversations = conversations.map { ConversationDto(it.id, userId, it.participantName, it.lastMessage, it.lastMessageTime, it.estado, it.isPinned, it.updatedAt) }
                    )
                    
                    val response = api.pushData("Bearer token", syncPackage)
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(syncStatus = "Nube actualizada con tu ID")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(syncStatus = "Bajando tus datos...")
                    val response = api.pullData("Bearer token", userId, 0)
                    if (response.isSuccessful) {
                        val data = response.body()
                        db.phraseDao().deleteAllPhrasesByUsuario(userId)
                        db.phraseDao().deleteAllPhrasesByUsuario("default_user") // Limpieza extra
                        db.conversationDao().deleteAllConversationsByUsuario(userId)
                        db.conversationDao().deleteAllConversationsByUsuario("default_user") // Limpieza extra
                        
                        data?.phrases?.forEach { db.phraseDao().insertPhrase(it.toEntity()) }
                        data?.conversations?.forEach { db.conversationDao().insertConversation(it.toEntity()) }
                        
                        _uiState.value = _uiState.value.copy(syncStatus = "¡Todo restaurado!")
                    }
                }
            } catch (e: Exception) {
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
    fun setTtsSpeed(speed: TtsSpeed) { _uiState.value = _uiState.value.copy(ttsSpeed = speed) }
    fun toggleHapticAlerts(enabled: Boolean) { _uiState.value = _uiState.value.copy(hapticAlerts = enabled) }
    fun toggleLocalHistory(enabled: Boolean) { _uiState.value = _uiState.value.copy(localHistory = enabled) }
    fun downloadOfflinePackage() { }
    fun logout(onComplete: () -> Unit) { onComplete() }

    class Factory(private val application: Application, private val ttsManager: TextToSpeechManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(application, ttsManager) as T
    }
}

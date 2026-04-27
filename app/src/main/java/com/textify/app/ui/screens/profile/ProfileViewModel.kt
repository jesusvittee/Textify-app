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
import com.textify.app.utils.NetworkUtils
import com.textify.app.ai.models.AiApiClient
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
    val autoCleanupDays: Int = 30,
    val appLanguage: String = "Español (México)",
    val femaleVoices: List<VoiceOption> = emptyList(),
    val maleVoices: List<VoiceOption> = emptyList(),
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

    private val allFemaleVoices = listOf(
        VoiceOption(Constants.VOICE_ID_SARAH, "Sarah (IA)", "Mature, Reassuring", "S", true),
        VoiceOption(Constants.VOICE_ID_LAURA, "Laura (IA)", "Enthusiast, Quirky", "L", true),
        VoiceOption(Constants.VOICE_ID_ALICE, "Alice (IA)", "Clear, Educator", "A", true),
        VoiceOption(Constants.VOICE_ID_MATILDA, "Matilda (IA)", "Professional", "M", true),
        VoiceOption(Constants.VOICE_ID_JESSICA, "Jessica (IA)", "Playful, Bright", "J", true),
        VoiceOption(Constants.VOICE_ID_BELLA, "Bella (IA)", "Professional, Bright", "B", true),
        VoiceOption(Constants.VOICE_ID_LILY, "Lily (IA)", "Velvety Actress", "L", true),
        VoiceOption(Constants.VOICE_ID_SUSANA, "Susana (IA)", "Warm, Soft", "S", true),
        VoiceOption(Constants.VOICE_ID_MAYA, "Maya (IA)", "Dynamic Storyteller", "M", true)
    )

    private val allMaleVoices = listOf(
        VoiceOption(Constants.VOICE_ID_ADAM, "Adam (IA)", "Dominant, Firm", "A", true),
        VoiceOption(Constants.VOICE_ID_ROGER, "Roger (IA)", "Laid-Back, Casual", "R", true),
        VoiceOption(Constants.VOICE_ID_CHARLIE, "Charlie (IA)", "Deep, Confident", "C", true),
        VoiceOption(Constants.VOICE_ID_GEORGE, "George (IA)", "Captivating Storyteller", "G", true),
        VoiceOption(Constants.VOICE_ID_CALLUM, "Callum (IA)", "Husky Trickster", "C", true),
        VoiceOption(Constants.VOICE_ID_RIVER, "River (IA)", "Relaxed, Neutral", "R", true),
        VoiceOption(Constants.VOICE_ID_HARRY, "Harry (IA)", "Fierce Warrior", "H", true),
        VoiceOption(Constants.VOICE_ID_LIAM, "Liam (IA)", "Social Media Creator", "L", true),
        VoiceOption(Constants.VOICE_ID_WILL, "Will (IA)", "Relaxed Optimist", "W", true),
        VoiceOption(Constants.VOICE_ID_ERIC, "Eric (IA)", "Smooth, Trustworthy", "E", true),
        VoiceOption(Constants.VOICE_ID_CHRIS, "Chris (IA)", "Charming", "C", true),
        VoiceOption(Constants.VOICE_ID_DANIEL, "Daniel (IA)", "Steady Broadcaster", "D", true),
        VoiceOption(Constants.VOICE_ID_BILL, "Bill (IA)", "Wise, Mature", "B", true)
    )

    private val offlineVoice = VoiceOption("default_offline", "Sistema (Offline)", "Predeterminada", "S", false)

    init { 
        loadUserData()
        loadSettings()
        refreshVoices()
    }

    private fun refreshVoices() {
        val isOnline = NetworkUtils.isOnline(getApplication())
        val currentGender = _uiState.value.voiceGender
        
        val females = if (isOnline) allFemaleVoices else listOf(offlineVoice)
        val males = if (isOnline) allMaleVoices else listOf(offlineVoice)
        
        _uiState.value = _uiState.value.copy(
            femaleVoices = females,
            maleVoices = males
        )

        // Si hay internet y la voz actual es offline, cambiar a Sarah o Adam
        if (isOnline && _uiState.value.selectedVoiceId == "default_offline") {
            val defaultVoiceId = if (currentGender == VoiceGender.FEMALE) Constants.VOICE_ID_SARAH else Constants.VOICE_ID_ADAM
            setSelectedVoice(defaultVoiceId)
        } 
        // Si NO hay internet, forzar la voz offline
        else if (!isOnline) {
            setSelectedVoice("default_offline")
        }
    }

    private fun loadSettings() {
        val savedVoiceId = prefs.getString(Constants.KEY_SELECTED_VOICE_ID, "default_offline") ?: "default_offline"
        val savedGenderStr = prefs.getString(Constants.KEY_VOICE_GENDER, VoiceGender.MALE.name) ?: VoiceGender.MALE.name
        val savedGender = try { VoiceGender.valueOf(savedGenderStr) } catch(e: Exception) { VoiceGender.MALE }
        
        _uiState.value = _uiState.value.copy(
            selectedVoiceId = savedVoiceId,
            voiceGender = savedGender
        )
    }

    private fun loadUserData() {
        viewModelScope.launch {
            db.usuarioDao().getUsuario().collect { user ->
                user?.let { _uiState.value = _uiState.value.copy(userId = it.id, name = it.nombre, email = it.correo) }
            }
        }
    }

    fun playVoicePreview(voice: VoiceOption) {
        ttsManager.speak("Esta es mi voz", voice.id, _uiState.value.voiceGender)
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
                    _uiState.value = _uiState.value.copy(syncStatus = "Vaciando nube...")
                    try {
                        api.clearData("Bearer $token", userId)
                    } catch (e: Exception) {
                        Log.w("Sync", "Aviso al limpiar: ${e.message}")
                    }

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

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            val token = prefs.getString(Constants.KEY_USER_TOKEN, "") ?: ""

            if (userId.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(isSyncing = true, syncStatus = "Eliminando cuenta...")
            try {
                val response = api.deleteAccount("Bearer $token", userId)
                if (response.isSuccessful) {
                    // Limpiar datos locales
                    db.usuarioDao().clearUsuarios()
                    db.phraseDao().deleteAllPhrasesByUsuario(userId)
                    db.conversationDao().deleteAllConversationsByUsuario(userId)
                    db.messageDao().deleteAllMessagesByUsuario(userId)
                    
                    prefs.edit().clear().apply()
                    onSuccess()
                } else {
                    Log.e("Auth", "Error al eliminar cuenta: ${response.code()}")
                    _uiState.value = _uiState.value.copy(syncStatus = "Error al eliminar")
                }
            } catch (e: Exception) {
                Log.e("Auth", "Excepción al eliminar cuenta", e)
                _uiState.value = _uiState.value.copy(syncStatus = "Error de conexión")
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) { _uiState.value = _uiState.value.copy(isDarkMode = enabled) }
    fun setFontScale(scale: Float) { _uiState.value = _uiState.value.copy(fontScale = scale) }
    
    fun setVoiceGender(gender: VoiceGender) { 
        _uiState.value = _uiState.value.copy(voiceGender = gender)
        prefs.edit().putString(Constants.KEY_VOICE_GENDER, gender.name).apply()
        refreshVoices()
    }
    
    fun setSelectedVoice(id: String) { 
        _uiState.value = _uiState.value.copy(selectedVoiceId = id)
        prefs.edit().putString(Constants.KEY_SELECTED_VOICE_ID, id).apply()
    }

    fun downloadOfflinePackage() { }
    fun logout(onComplete: () -> Unit) { 
        prefs.edit().clear().apply()
        onComplete() 
    }

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

package com.textify.app.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.textify.app.services.audio.OfflineAudioService
import com.textify.app.services.audio.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    val downloadProgress: String = ""
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState
    
    private val offlineAudioService = OfflineAudioService(application)
    private val ttsManager = TextToSpeechManager(application)

    init {
        _uiState.value = _uiState.value.copy(
            isOfflinePackageInstalled = offlineAudioService.isModelReady()
        )
    }

    fun downloadOfflinePackage() {
        if (_uiState.value.isDownloading) return
        
        _uiState.value = _uiState.value.copy(isDownloading = true, downloadProgress = "Iniciando...")
        
        offlineAudioService.downloadModel(
            onProgress = { progress ->
                _uiState.value = _uiState.value.copy(downloadProgress = progress)
            },
            onComplete = { success ->
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    isOfflinePackageInstalled = success,
                    downloadProgress = if (success) "¡Completado!" else "Error en la descarga"
                )
            }
        )
    }

    fun setVoiceGender(gender: VoiceGender) {
        _uiState.value = _uiState.value.copy(voiceGender = gender, selectedVoiceId = "default_offline")
    }

    fun setSelectedVoice(voiceId: String) {
        _uiState.value = _uiState.value.copy(selectedVoiceId = voiceId)
        // Opcional: Probar la voz al seleccionarla
        val textToSample = "Hola, esta es mi nueva voz."
        ttsManager.speak(textToSample, voiceId, _uiState.value.voiceGender)
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun setFontScale(scale: Float) {
        _uiState.value = _uiState.value.copy(fontScale = scale)
    }

    fun setTtsSpeed(speed: TtsSpeed) {
        _uiState.value = _uiState.value.copy(ttsSpeed = speed)
    }

    fun toggleHapticAlerts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hapticAlerts = enabled)
    }

    fun toggleLocalHistory(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(localHistory = enabled)
    }

    fun logout(onSuccess: () -> Unit) {
        onSuccess()
    }
}

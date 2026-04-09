package com.textify.app.ui.screens.profile

import androidx.lifecycle.ViewModel
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
    val initial: String
)

data class ProfileUiState(
    val name: String = "Nicolás Vite",
    val email: String = "nicolas@example.com",
    val userType: UserType = UserType.DEAF,
    val isDarkMode: Boolean = false,
    val highContrast: Boolean = true,
    val fontSize: FontSize = FontSize.MEDIUM,
    val fontScale: Float = 1.0f,
    val voiceGender: VoiceGender = VoiceGender.FEMALE,
    val selectedVoiceId: String = "sofia",
    val ttsSpeed: TtsSpeed = TtsSpeed.NORMAL,
    val hapticAlerts: Boolean = true,
    val localHistory: Boolean = true,
    val autoCleanupDays: Int = 30,
    val appLanguage: String = "Español (México)",
    val femaleVoices: List<VoiceOption> = listOf(
        VoiceOption("sofia", "Sofía", "Voz suave y cálida", "S"),
        VoiceOption("valentina", "Valentina", "Voz clara y natural", "V"),
        VoiceOption("camila", "Camila", "Voz expresiva y amigable", "C")
    ),
    val maleVoices: List<VoiceOption> = listOf(
        VoiceOption("alejandro", "Alejandro", "Voz grave y firme", "A"),
        VoiceOption("diego", "Diego", "Voz clara y profesional", "D"),
        VoiceOption("mateo", "Mateo", "Voz amigable y cercana", "M")
    )
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun setUserType(type: UserType) {
        _uiState.value = _uiState.value.copy(userType = type)
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun toggleHighContrast(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(highContrast = enabled)
    }

    fun setFontSize(size: FontSize) {
        val scale = when(size) {
            FontSize.SMALL -> 0.85f
            FontSize.MEDIUM -> 1.0f
            FontSize.LARGE -> 1.3f
        }
        _uiState.value = _uiState.value.copy(fontSize = size, fontScale = scale)
    }
    
    fun setFontScale(scale: Float) {
        _uiState.value = _uiState.value.copy(fontScale = scale)
    }

    fun setVoiceGender(gender: VoiceGender) {
        val defaultId = if (gender == VoiceGender.FEMALE) "sofia" else "alejandro"
        _uiState.value = _uiState.value.copy(voiceGender = gender, selectedVoiceId = defaultId)
    }

    fun setSelectedVoice(voiceId: String) {
        _uiState.value = _uiState.value.copy(selectedVoiceId = voiceId)
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

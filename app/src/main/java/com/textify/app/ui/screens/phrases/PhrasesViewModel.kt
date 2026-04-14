package com.textify.app.ui.screens.phrases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.textify.app.domain.model.Phrase
import com.textify.app.domain.usecase.AddPhraseUseCase
import com.textify.app.domain.usecase.DeletePhraseUseCase
import com.textify.app.domain.usecase.GetPhrasesUseCase
import com.textify.app.domain.usecase.PlayPhraseUseCase
import com.textify.app.ui.screens.profile.VoiceGender
import com.textify.app.data.local.database.AppDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class PhrasesUiState(
    val userId: String = "default_user",
    val phrases: List<Phrase> = emptyList(),
    val selectedPhraseIds: Set<String> = emptySet(),
    val isPlaying: Boolean = false,
    val playingPhraseId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedVoiceId: String = "default_offline",
    val voiceGender: VoiceGender = VoiceGender.MALE
) {
    val isSelectionMode: Boolean get() = selectedPhraseIds.isNotEmpty()
}

class PhrasesViewModel(
    application: Application,
    private val getPhrasesUseCase: GetPhrasesUseCase,
    private val addPhraseUseCase: AddPhraseUseCase,
    private val deletePhraseUseCase: DeletePhraseUseCase,
    private val playPhraseUseCase: PlayPhraseUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PhrasesUiState())
    val uiState: StateFlow<PhrasesUiState> = _uiState
    
    private val db = (application as com.textify.app.TextifyApp).database

    init {
        loadUserData()
        loadPhrases()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            db.usuarioDao().getUsuario().collect { user ->
                user?.let {
                    _uiState.value = _uiState.value.copy(userId = it.id)
                }
            }
        }
    }

    fun updateVoiceSettings(voiceId: String, gender: VoiceGender) {
        _uiState.value = _uiState.value.copy(
            selectedVoiceId = voiceId,
            voiceGender = gender
        )
    }

    private fun loadPhrases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val phrases = getPhrasesUseCase()
                _uiState.value = _uiState.value.copy(phrases = phrases, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun addPhrase(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val newPhrase = Phrase(
                    id = UUID.randomUUID().toString(),
                    text = text,
                    usuarioId = _uiState.value.userId
                )
                addPhraseUseCase(newPhrase)
                loadPhrases()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deletePhrase(phraseId: String) {
        viewModelScope.launch {
            try {
                deletePhraseUseCase(phraseId)
                loadPhrases()
                _uiState.value = _uiState.value.copy(
                    selectedPhraseIds = _uiState.value.selectedPhraseIds - phraseId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteSelectedPhrases() {
        viewModelScope.launch {
            try {
                _uiState.value.selectedPhraseIds.forEach { id ->
                    deletePhraseUseCase(id)
                }
                _uiState.value = _uiState.value.copy(selectedPhraseIds = emptySet())
                loadPhrases()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleSelection(phraseId: String) {
        val currentSelected = _uiState.value.selectedPhraseIds
        val newSelected = if (currentSelected.contains(phraseId)) {
            currentSelected - phraseId
        } else {
            currentSelected + phraseId
        }
        _uiState.value = _uiState.value.copy(selectedPhraseIds = newSelected)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedPhraseIds = emptySet())
    }

    fun setPlaying(phraseId: String?) {
        val phrase = _uiState.value.phrases.find { it.id == phraseId }
        phrase?.let {
            playPhraseUseCase(it, _uiState.value.selectedVoiceId, _uiState.value.voiceGender)
            _uiState.value = _uiState.value.copy(
                isPlaying = true,
                playingPhraseId = phraseId
            )
        } ?: run {
            _uiState.value = _uiState.value.copy(
                isPlaying = false,
                playingPhraseId = null
            )
        }
    }

    class Factory(
        private val application: Application,
        private val getPhrasesUseCase: GetPhrasesUseCase,
        private val addPhraseUseCase: AddPhraseUseCase,
        private val deletePhraseUseCase: DeletePhraseUseCase,
        private val playPhraseUseCase: PlayPhraseUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhrasesViewModel(
                application,
                getPhrasesUseCase,
                addPhraseUseCase,
                deletePhraseUseCase,
                playPhraseUseCase
            ) as T
        }
    }
}

package com.textify.app.ui.screens.phrases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.textify.app.domain.model.Phrase
import com.textify.app.domain.usecase.AddPhraseUseCase
import com.textify.app.domain.usecase.DeletePhraseUseCase
import com.textify.app.domain.usecase.GetPhrasesUseCase
import com.textify.app.domain.usecase.PlayPhraseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class PhrasesUiState(
    val phrases: List<Phrase> = emptyList(),
    val isPlaying: Boolean = false,
    val playingPhraseId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PhrasesViewModel(
    private val getPhrasesUseCase: GetPhrasesUseCase,
    private val addPhraseUseCase: AddPhraseUseCase,
    private val deletePhraseUseCase: DeletePhraseUseCase,
    private val playPhraseUseCase: PlayPhraseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhrasesUiState())
    val uiState: StateFlow<PhrasesUiState> = _uiState

    init {
        loadPhrases()
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
                    text = text
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setPlaying(phraseId: String?) {
        val phrase = _uiState.value.phrases.find { it.id == phraseId }
        phrase?.let {
            playPhraseUseCase(it)
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
        private val getPhrasesUseCase: GetPhrasesUseCase,
        private val addPhraseUseCase: AddPhraseUseCase,
        private val deletePhraseUseCase: DeletePhraseUseCase,
        private val playPhraseUseCase: PlayPhraseUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhrasesViewModel(
                getPhrasesUseCase,
                addPhraseUseCase,
                deletePhraseUseCase,
                playPhraseUseCase
            ) as T
        }
    }
}

package com.textify.app.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.textify.app.ai.stt.SpeechRecognizer
import com.textify.app.data.local.entity.ConversationEntity
import com.textify.app.domain.model.Message
import com.textify.app.domain.model.MessageType
import com.textify.app.domain.model.Gender
import com.textify.app.domain.usecase.*
import com.textify.app.services.audio.TextToSpeechManager
import com.textify.app.ui.screens.profile.VoiceGender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val conversations: List<ConversationEntity> = emptyList(),
    val currentConversation: ConversationEntity? = null,
    val isListening: Boolean = false,
    val isRecording: Boolean = false,
    val isLoading: Boolean = false,
    val isOnline: Boolean = false,
    val error: String? = null,
    val transcribedText: String = "",
    val selectedVoiceId: String = "default_offline",
    val voiceGender: VoiceGender = VoiceGender.MALE
)

class ChatViewModel(
    application: Application,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val speechRecognizer: SpeechRecognizer,
    private val getConversationsUseCase: GetConversationsUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val ttsManager = TextToSpeechManager(application)

    init {
        loadConversations()
    }

    fun updateVoiceSettings(voiceId: String, gender: VoiceGender) {
        _uiState.value = _uiState.value.copy(
            selectedVoiceId = voiceId,
            voiceGender = gender
        )
    }

    private fun loadConversations() {
        viewModelScope.launch {
            getConversationsUseCase().collectLatest { list ->
                val filteredList = list.filter { it.lastMessage.isNotEmpty() }
                _uiState.value = _uiState.value.copy(conversations = filteredList)
                if (_uiState.value.currentConversation == null) {
                    createNewConversation("Textify")
                }
            }
        }
    }

    fun playMessage(text: String) {
        ttsManager.speak(text, _uiState.value.selectedVoiceId, _uiState.value.voiceGender)
    }

    fun sendMessage(text: String, type: MessageType = MessageType.TEXT, isOwn: Boolean = true) {
        if (text.isBlank()) return
        val currentConversation = _uiState.value.currentConversation ?: return
        val currentId = currentConversation.id

        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = currentId,
            text = text,
            isOwn = isOwn,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                if (_uiState.value.messages.isEmpty()) {
                    val updatedName = if (currentConversation.participantName == "Textify") text.take(30) else currentConversation.participantName
                    val convToSave = currentConversation.copy(
                        participantName = updatedName,
                        lastMessage = text,
                        lastMessageTime = System.currentTimeMillis()
                    )
                    createConversationUseCase(convToSave)
                    _uiState.value = _uiState.value.copy(currentConversation = convToSave)
                }
                sendMessageUseCase(message)
                updateConversationUseCase(ConversationEntity(
                    id = currentId,
                    participantName = _uiState.value.currentConversation?.participantName ?: "Textify",
                    lastMessage = text,
                    lastMessageTime = System.currentTimeMillis()
                ))
                loadMessages(currentId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val messages = getMessagesUseCase(conversationId)
                _uiState.value = _uiState.value.copy(messages = messages, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun selectConversation(id: String) {
        val conversation = _uiState.value.conversations.find { it.id == id }
        if (conversation != null) {
            _uiState.value = _uiState.value.copy(currentConversation = conversation)
            loadMessages(id)
        }
    }

    fun createConversation(title: String) {
        viewModelScope.launch {
            val newConv = ConversationEntity(
                id = UUID.randomUUID().toString(),
                participantName = title,
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis()
            )
            createConversationUseCase(newConv)
            _uiState.value = _uiState.value.copy(currentConversation = newConv, messages = emptyList())
        }
    }
    
    fun createNewConversation(title: String) {
        viewModelScope.launch {
            val newConv = ConversationEntity(
                id = UUID.randomUUID().toString(),
                participantName = title,
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis()
            )
            _uiState.value = _uiState.value.copy(currentConversation = newConv, messages = emptyList())
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch { deleteConversationUseCase(id) }
    }

    fun renameConversation(id: String, newName: String) {
        viewModelScope.launch {
            _uiState.value.conversations.find { it.id == id }?.let {
                updateConversationUseCase(it.copy(participantName = newName))
            }
        }
    }

    fun togglePinConversation(id: String) {
        viewModelScope.launch {
            _uiState.value.conversations.find { it.id == id }?.let {
                updateConversationUseCase(it.copy(isPinned = !it.isPinned))
            }
        }
    }

    fun startListening() {
        _uiState.value = _uiState.value.copy(isListening = true, isRecording = false, transcribedText = "", error = null)
        speechRecognizer.startListening(
            onReady = { _uiState.value = _uiState.value.copy(isRecording = true) },
            onResult = { result -> _uiState.value = _uiState.value.copy(transcribedText = result) },
            onError = { errorMsg -> _uiState.value = _uiState.value.copy(isListening = false, isRecording = false, error = errorMsg) }
        )
    }

    fun stopAndSend() {
        speechRecognizer.stopListening()
        val textToSend = _uiState.value.transcribedText
        if (textToSend.isNotBlank()) {
            sendMessage(textToSend, type = MessageType.AUDIO, isOwn = false)
        }
        _uiState.value = _uiState.value.copy(isListening = false, isRecording = false, transcribedText = "")
    }

    fun cancelListening() {
        speechRecognizer.stopListening()
        _uiState.value = _uiState.value.copy(isListening = false, isRecording = false, transcribedText = "")
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }

    class Factory(
        private val application: Application,
        private val getMessagesUseCase: GetMessagesUseCase,
        private val sendMessageUseCase: SendMessageUseCase,
        private val speechRecognizer: SpeechRecognizer,
        private val getConversationsUseCase: GetConversationsUseCase,
        private val createConversationUseCase: CreateConversationUseCase,
        private val updateConversationUseCase: UpdateConversationUseCase,
        private val deleteConversationUseCase: DeleteConversationUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(application, getMessagesUseCase, sendMessageUseCase, speechRecognizer, getConversationsUseCase, createConversationUseCase, updateConversationUseCase, deleteConversationUseCase) as T
        }
    }
}

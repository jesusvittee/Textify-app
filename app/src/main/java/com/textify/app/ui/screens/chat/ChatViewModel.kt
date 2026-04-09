package com.textify.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.textify.app.ai.stt.SpeechRecognizer
import com.textify.app.data.local.entity.ConversationEntity
import com.textify.app.domain.model.Message
import com.textify.app.domain.usecase.*
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
    val recordingDuration: String = "0:00"
)

class ChatViewModel(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val speechRecognizer: SpeechRecognizer,
    private val getConversationsUseCase: GetConversationsUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        loadConversations()
        _uiState.value = _uiState.value.copy(isOnline = false)
    }

    private fun loadConversations() {
        viewModelScope.launch {
            getConversationsUseCase().collectLatest { list ->
                // Solo mostramos conversaciones que tengan al menos un mensaje
                val filteredList = list.filter { it.lastMessage.isNotEmpty() }
                _uiState.value = _uiState.value.copy(conversations = filteredList)
                
                // Al iniciar la app (cuando currentConversation es null), 
                // siempre empezamos con una nueva conversación vacía por defecto
                if (_uiState.value.currentConversation == null) {
                    createNewConversation("Textify")
                }
            }
        }
    }

    fun selectConversation(conversation: ConversationEntity) {
        _uiState.value = _uiState.value.copy(currentConversation = conversation)
        loadMessages(conversation.id)
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

    fun createNewConversation(title: String) {
        viewModelScope.launch {
            val newConv = ConversationEntity(
                id = UUID.randomUUID().toString(),
                participantName = title,
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis()
            )
            // No la guardamos en BD todavía para que no aparezca en la lista si está vacía
            _uiState.value = _uiState.value.copy(currentConversation = newConv, messages = emptyList())
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            deleteConversationUseCase(id)
        }
    }

    fun renameConversation(id: String, newName: String) {
        viewModelScope.launch {
            val conv = _uiState.value.conversations.find { it.id == id }
            conv?.let {
                updateConversationUseCase(it.copy(participantName = newName))
            }
        }
    }

    fun togglePinConversation(id: String) {
        viewModelScope.launch {
            val conv = _uiState.value.conversations.find { it.id == id }
            conv?.let {
                updateConversationUseCase(it.copy(isPinned = !it.isPinned))
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentConversation = _uiState.value.currentConversation ?: return
        val currentId = currentConversation.id

        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = currentId,
            text = text,
            isOwn = true,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                // Si es el primer mensaje, primero creamos la conversación en la BD
                if (_uiState.value.messages.isEmpty()) {
                    val updatedName = if (currentConversation.participantName == "Textify") {
                        text.take(30)
                    } else {
                        currentConversation.participantName
                    }
                    val convToSave = currentConversation.copy(
                        participantName = updatedName,
                        lastMessage = text,
                        lastMessageTime = System.currentTimeMillis()
                    )
                    createConversationUseCase(convToSave)
                    _uiState.value = _uiState.value.copy(currentConversation = convToSave)
                }

                sendMessageUseCase(message)
                updateConversationData(currentId, text, _uiState.value.currentConversation?.participantName ?: "Textify")
                loadMessages(currentId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private suspend fun updateConversationData(id: String, lastMsg: String, name: String) {
        updateConversationUseCase(ConversationEntity(
            id = id,
            participantName = name,
            lastMessage = lastMsg,
            lastMessageTime = System.currentTimeMillis()
        ))
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        _uiState.value = _uiState.value.copy(isListening = true, isRecording = true, error = null)
        speechRecognizer.startListening(
            onResult = { result ->
                _uiState.value = _uiState.value.copy(isListening = false, isRecording = false, transcribedText = result)
                sendMessage(result)
            },
            onError = { errorMsg ->
                _uiState.value = _uiState.value.copy(isListening = false, isRecording = false, error = errorMsg)
            }
        )
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        _uiState.value = _uiState.value.copy(isListening = false, isRecording = false)
    }

    class Factory(
        private val getMessagesUseCase: GetMessagesUseCase,
        private val sendMessageUseCase: SendMessageUseCase,
        private val speechRecognizer: SpeechRecognizer,
        private val getConversationsUseCase: GetConversationsUseCase,
        private val createConversationUseCase: CreateConversationUseCase,
        private val updateConversationUseCase: UpdateConversationUseCase,
        private val deleteConversationUseCase: DeleteConversationUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(
                getMessagesUseCase,
                sendMessageUseCase,
                speechRecognizer,
                getConversationsUseCase,
                createConversationUseCase,
                updateConversationUseCase,
                deleteConversationUseCase
            ) as T
        }
    }
}
package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskerine_v2.data.model.Message
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessageViewModel(private val repository: TaskerineRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun loadMessages(taskId: String) {
        viewModelScope.launch {
            repository.getMessagesForTask(taskId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(taskId: String, senderId: String, senderName: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                Message(
                    id = "m${System.currentTimeMillis()}",
                    taskId = taskId,
                    senderId = senderId,
                    senderName = senderName,
                    content = content.trim()
                )
            )
        }
    }

    /**
     * Returns a live Flow of messages for a specific task.
     * Used by the inbox screen to show last message previews
     * for each conversation row without loading everything into
     * a single shared StateFlow.
     */
    fun getMessagesForTask(taskId: String): Flow<List<Message>> =
        repository.getMessagesForTask(taskId)
}

class MessageViewModelFactory(private val repository: TaskerineRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MessageViewModel(repository) as T
    }
}
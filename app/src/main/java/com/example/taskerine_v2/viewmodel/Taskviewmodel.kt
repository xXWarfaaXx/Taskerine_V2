package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerine_v2.data.model.Message
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskerineRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<Task>> = combine(allTasks, _searchQuery) { tasks, query ->
        if (query.isBlank()) tasks
        else tasks.filter {
            it.title.lowercase().contains(query.lowercase()) ||
                    it.description.lowercase().contains(query.lowercase()) ||
                    it.location.lowercase().contains(query.lowercase())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _myTasks = MutableStateFlow<List<Task>>(emptyList())
    val myTasks: StateFlow<List<Task>> = _myTasks

    val filteredMyTasks: StateFlow<List<Task>> = combine(_myTasks, _searchQuery) { tasks, query ->
        if (query.isBlank()) tasks
        else tasks.filter {
            it.title.lowercase().contains(query.lowercase()) ||
                    it.description.lowercase().contains(query.lowercase()) ||
                    it.location.lowercase().contains(query.lowercase())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    // --- Completion flow state ---
    private val _completionError = MutableStateFlow<String?>(null)
    val completionError: StateFlow<String?> = _completionError

    private val _completionSuccess = MutableStateFlow(false)
    val completionSuccess: StateFlow<Boolean> = _completionSuccess

    fun loadMyTasks(userId: String) {
        viewModelScope.launch {
            repository.getTasksForUser(userId).collect {
                _myTasks.value = it
            }
        }
    }

    fun selectTask(taskId: String) {
        viewModelScope.launch {
            _selectedTask.value = repository.getTaskById(taskId)
        }
    }

    fun postTask(task: Task) {
        viewModelScope.launch { repository.postTask(task) }
    }

    fun acceptTask(taskId: String, taskerId: String) {
        viewModelScope.launch {
            // Reserve coins from requester when task is accepted
            val task = repository.getTaskById(taskId) ?: return@launch
            val rewardCoins = task.reward.toInt()
            repository.deductCoins(task.requesterId, rewardCoins)
            repository.acceptTask(taskId, taskerId)
            _selectedTask.value = repository.getTaskById(taskId)
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch { repository.completeTask(taskId) }
    }

    /**
     * Tasker calls this when they believe the task is done.
     * Sends a system message in Task Chat notifying the Requester to confirm.
     */
    fun requestCompletion(
        taskId: String,
        taskerName: String,
        onMessageSent: () -> Unit
    ) {
        viewModelScope.launch {
            repository.sendMessage(
                Message(
                    id = "sys_${System.currentTimeMillis()}",
                    taskId = taskId,
                    senderId = "system",
                    senderName = "System",
                    content = "✅ $taskerName has marked this task as complete. " +
                            "Please confirm below to release payment.",
                    timestamp = System.currentTimeMillis()
                )
            )
            onMessageSent()
        }
    }

    /**
     * Requester calls this to confirm completion.
     * Coins were already deducted at acceptance, so this just
     * transfers them to the Tasker and marks the task Completed.
     */
    fun completeTaskWithPayment(
        taskId: String,
        taskerId: String,
        rewardAmount: Int
    ) {
        viewModelScope.launch {
            // Coins were held at acceptance — now release them to the Tasker.
            repository.addCoins(taskerId, rewardAmount)
            repository.completeTask(taskId)
            _selectedTask.value = repository.getTaskById(taskId)
            _completionError.value = null
            _completionSuccess.value = true
        }
    }

    fun clearCompletionState() {
        _completionError.value = null
        _completionSuccess.value = false
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
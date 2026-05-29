package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        viewModelScope.launch { repository.acceptTask(taskId, taskerId) }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch { repository.completeTask(taskId) }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
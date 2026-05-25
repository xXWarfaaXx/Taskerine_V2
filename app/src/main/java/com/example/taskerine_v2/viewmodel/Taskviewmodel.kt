package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.TaskStatus
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel : ViewModel() {

    private val _openTasks = MutableStateFlow<List<Task>>(emptyList())
    val openTasks: StateFlow<List<Task>> = _openTasks

    private val _myTasks = MutableStateFlow<List<Task>>(emptyList())
    val myTasks: StateFlow<List<Task>> = _myTasks

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())
    val filteredTasks: StateFlow<List<Task>> = _filteredTasks

    private val _filteredMyTasks = MutableStateFlow<List<Task>>(emptyList())
    val filteredMyTasks: StateFlow<List<Task>> = _filteredMyTasks

    fun loadOpenTasks() {
        _openTasks.value = TaskerineRepository.getOpenTasks()
        applySearch()
    }

    fun loadMyTasks(userId: String) {
        _myTasks.value = TaskerineRepository.getTasksForUser(userId)
        applyMyTasksSearch()
    }

    fun selectTask(taskId: String) {
        _selectedTask.value = TaskerineRepository.getTaskById(taskId)
    }

    fun postTask(task: Task) {
        TaskerineRepository.postTask(task)
        loadOpenTasks()
    }

    fun acceptTask(taskId: String, taskerId: String) {
        TaskerineRepository.acceptTask(taskId, taskerId)
        loadOpenTasks()
        loadMyTasks(taskerId)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applySearch()
        applyMyTasksSearch()
    }

    private fun applySearch() {
        val q = _searchQuery.value.trim().lowercase()
        _filteredTasks.value = if (q.isEmpty()) {
            _openTasks.value
        } else {
            _openTasks.value.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.location.lowercase().contains(q)
            }
        }
    }

    private fun applyMyTasksSearch() {
        val q = _searchQuery.value.trim().lowercase()
        _filteredMyTasks.value = if (q.isEmpty()) {
            _myTasks.value
        } else {
            _myTasks.value.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.location.lowercase().contains(q)
            }
        }
    }
}
package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.taskerine_v2.data.model.Task
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

    fun loadOpenTasks() {
        _openTasks.value = TaskerineRepository.getOpenTasks()
    }

    fun loadMyTasks(userId: String) {
        _myTasks.value = TaskerineRepository.getTasksForUser(userId)
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
}
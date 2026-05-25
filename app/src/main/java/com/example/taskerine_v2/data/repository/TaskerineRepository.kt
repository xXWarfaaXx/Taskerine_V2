package com.example.taskerine_v2.data.repository

import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.TaskStatus
import com.example.taskerine_v2.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TaskerineRepository {

    private val _users = mutableListOf(
        User("u1", "alice", "alice@email.com", Role.REQUESTER),
        User("u2", "bob", "bob@email.com", Role.TASKER)
    )

    private var _currentUser: User? = null
    val currentUser get() = _currentUser

    fun login(email: String, password: String): User? {
        _currentUser = _users.find { it.email == email }
        return _currentUser
    }

    fun register(username: String, email: String, role: Role): User {
        val user = User(
            id = "u${_users.size + 1}",
            username = username,
            email = email,
            role = role
        )
        _users.add(user)
        _currentUser = user
        return user
    }

    fun logout() {
        _currentUser = null
    }

    private val _tasks = MutableStateFlow(
        mutableListOf(
            Task(
                id = "t1",
                title = "Help move furniture",
                description = "Need help moving a sofa and dining table to a new flat nearby.",
                location = "Brixton, London",
                reward = 40.0,
                requesterId = "u1",
                requesterName = "alice"
            ),
            Task(
                id = "t2",
                title = "Grocery shopping",
                description = "Pick up a list of groceries from Sainsbury's and drop off.",
                location = "Hackney, London",
                reward = 15.0,
                requesterId = "u1",
                requesterName = "alice"
            )
        )
    )

    val tasks: StateFlow<MutableList<Task>> = _tasks

    fun getOpenTasks(): List<Task> = _tasks.value.toList() // returns ALL tasks so unavailable ones still show

    fun getTaskById(id: String): Task? = _tasks.value.find { it.id == id }

    fun postTask(task: Task) {
        val updated = _tasks.value.toMutableList()
        updated.add(task)
        _tasks.value = updated
    }

    fun acceptTask(taskId: String, taskerId: String) {
        val updated = _tasks.value.toMutableList()
        val index = updated.indexOfFirst { it.id == taskId }
        if (index != -1) {
            updated[index] = updated[index].copy(
                status = TaskStatus.ACCEPTED,
                acceptedById = taskerId
            )
        }
        _tasks.value = updated
    }

    fun getTasksForUser(userId: String): List<Task> {
        return _tasks.value.filter {
            it.requesterId == userId || it.acceptedById == userId
        }
    }
}
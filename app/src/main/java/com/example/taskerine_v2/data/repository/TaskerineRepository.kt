package com.example.taskerine_v2.data.repository

import com.example.taskerine_v2.data.model.Review
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.TaskStatus
import com.example.taskerine_v2.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TaskerineRepository {

    private val _users = mutableListOf(
        User("u1", "alice", "alice@email.com", Role.REQUESTER, coins = 200),
        User("u2", "bob", "bob@email.com", Role.TASKER, coins = 100)
    )

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUserFlow

    private var _currentUser: User? = null
    val currentUser get() = _currentUser

    fun login(email: String, password: String): User? {
        _currentUser = _users.find { it.email == email }
        _currentUserFlow.value = _currentUser
        return _currentUser
    }

    fun register(username: String, email: String, role: Role): User {
        val user = User(
            id = "u${_users.size + 1}",
            username = username,
            email = email,
            role = role,
            coins = 0
        )
        _users.add(user)
        _currentUser = user
        _currentUserFlow.value = user
        return user
    }

    fun logout() {
        _currentUser = null
        _currentUserFlow.value = null
    }

    fun addCoins(userId: String, amount: Int) {
        val index = _users.indexOfFirst { it.id == userId }
        if (index != -1) {
            val updated = _users[index].copy(coins = _users[index].coins + amount)
            _users[index] = updated
            if (_currentUser?.id == userId) {
                _currentUser = updated
                _currentUserFlow.value = updated
            }
        }
    }

    fun getCoins(userId: String): Int {
        return _users.find { it.id == userId }?.coins ?: 0
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

    fun getOpenTasks(): List<Task> = _tasks.value.toList()

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

    fun completeTask(taskId: String) {
        val updated = _tasks.value.toMutableList()
        val index = updated.indexOfFirst { it.id == taskId }
        if (index != -1) {
            updated[index] = updated[index].copy(status = TaskStatus.COMPLETED)
        }
        _tasks.value = updated
    }



    fun getTasksForUser(userId: String): List<Task> {
        return _tasks.value.filter {
            it.requesterId == userId || it.acceptedById == userId
        }
    }

    // --- Reviews ---
    private val _reviews = mutableListOf<Review>()

    fun submitReview(review: Review) {
        _reviews.add(review)
    }

    fun getReviewsForUser(userId: String): List<Review> {
        return _reviews.filter { it.targetUserId == userId }
    }

    fun getReviewForTask(taskId: String, reviewerId: String): Review? {
        return _reviews.find { it.taskId == taskId && it.reviewerId == reviewerId }
    }

    fun getAverageRating(userId: String): Double {
        val reviews = getReviewsForUser(userId)
        return if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
    }

    fun getUserById(userId: String): User? = _users.find { it.id == userId }
}
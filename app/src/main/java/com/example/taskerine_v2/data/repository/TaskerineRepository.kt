package com.example.taskerine_v2.data.repository

import com.example.taskerine_v2.data.local.TaskerineDatabase
import com.example.taskerine_v2.data.local.toEntity
import com.example.taskerine_v2.data.local.toModel
import com.example.taskerine_v2.data.local.entities.toEntity
import com.example.taskerine_v2.data.local.entities.toModel
import com.example.taskerine_v2.data.model.Report
import com.example.taskerine_v2.data.model.Review
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.TaskStatus
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.data.local.entities.UserEntity
import com.example.taskerine_v2.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class TaskerineRepository(private val db: TaskerineDatabase) {

    private val taskDao = db.taskDao()
    private val userDao = db.userDao()
    private val reviewDao = db.reviewDao()
    private val reportDao = db.reportDao()

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUserFlow

    // --- Tasks ---
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks().map { list ->
        list.map { it.toModel() }
    }

    fun getTasksForUser(userId: String): Flow<List<Task>> =
        taskDao.getTasksForUser(userId).map { list -> list.map { it.toModel() } }

    suspend fun getTaskById(id: String): Task? = taskDao.getTaskById(id)?.toModel()

    suspend fun postTask(task: Task) = taskDao.insertTask(task.toEntity())

    suspend fun acceptTask(taskId: String, taskerId: String) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.updateTask(
            task.copy(status = TaskStatus.ACCEPTED, acceptedById = taskerId)
        )
    }

    // Add to constructor/init:
    private val messageDao = db.messageDao()

    // Add these functions:
    fun getMessagesForTask(taskId: String): Flow<List<Message>> =
        messageDao.getMessagesForTask(taskId).map { list -> list.map { it.toModel() } }

    suspend fun sendMessage(message: Message) =
        messageDao.insertMessage(message.toEntity())

    suspend fun completeTask(taskId: String) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.updateTask(task.copy(status = TaskStatus.COMPLETED))
    }

    // --- Users ---
    suspend fun login(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email)?.toModel()
        _currentUserFlow.value = user
        return user
    }

    // Login by username instead of email.
    suspend fun loginByUsername(username: String): User? {
        val user = userDao.getUserByUsername(username)?.toModel()
        _currentUserFlow.value = user
        return user
    }

    suspend fun restoreSession(userId: String): User? {
        val user = userDao.getUserById(userId)?.toModel()
        _currentUserFlow.value = user
        return user
    }

    suspend fun register(username: String, email: String, role: Role): User {
        val user = User(
            id = "u${System.currentTimeMillis()}",
            username = username,
            email = email,
            role = role,
            coins = 0
        )
        userDao.insertUser(user.toEntity())
        _currentUserFlow.value = user
        return user
    }

    fun logout() {
        _currentUserFlow.value = null
    }

    suspend fun addCoins(userId: String, amount: Int) {
        val user = userDao.getUserById(userId) ?: return
        val updated = user.copy(coins = user.coins + amount)
        userDao.updateUser(updated)
        if (_currentUserFlow.value?.id == userId) {
            _currentUserFlow.value = updated.toModel()
        }
    }

    suspend fun getCoins(userId: String): Int =
        userDao.getUserById(userId)?.coins ?: 0

    suspend fun getUserById(userId: String): User? =
        userDao.getUserById(userId)?.toModel()

    suspend fun updateUserRole(userId: String, newRole: Role): User? {
        val user = userDao.getUserById(userId) ?: return null
        val updated = user.copy(role = newRole)
        userDao.updateUser(updated)
        if (_currentUserFlow.value?.id == userId) {
            _currentUserFlow.value = updated.toModel()
        }
        return updated.toModel()
    }

    // --- Profile updates ---
    // Returns Result.success(Unit) on success, or Result.failure with a message on validation failure.
    suspend fun updateUserProfile(
        userId: String,
        newUsername: String,
        newEmail: String
    ): Result<Unit> {
        if (newUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty"))
        }
        if (newEmail.isBlank() || !newEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter a valid email"))
        }

        val user = userDao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("User not found"))

        // Prevent duplicate emails across accounts (skip check if email is unchanged)
        if (newEmail != user.email) {
            val existing = userDao.getUserByEmail(newEmail)
            if (existing != null && existing.id != userId) {
                return Result.failure(IllegalArgumentException("Email already in use"))
            }
        }

        val updated = user.copy(username = newUsername.trim(), email = newEmail.trim())
        userDao.updateUser(updated)

        if (_currentUserFlow.value?.id == userId) {
            _currentUserFlow.value = updated.toModel()
        }
        return Result.success(Unit)
    }

    // --- Reviews ---
    fun getReviewsForUser(userId: String): Flow<List<Review>> =
        reviewDao.getReviewsForUser(userId).map { list -> list.map { it.toModel() } }

    suspend fun getReviewForTask(taskId: String, reviewerId: String): Review? =
        reviewDao.getReviewForTask(taskId, reviewerId)?.toModel()

    suspend fun submitReview(review: Review) =
        reviewDao.insertReview(review.toEntity())

    suspend fun getAverageRating(userId: String): Double {
        return reviewDao.getReviewsForUser(userId)
            .map { list ->
                if (list.isEmpty()) 0.0 else list.map { it.rating }.average()
            }
            .first()
    }

    // --- Reports ---
    suspend fun submitReport(report: Report) =
        reportDao.insertReport(report.toEntity())

    fun getReportsForUser(userId: String): Flow<List<Report>> =
        reportDao.getReportsForUser(userId).map { list -> list.map { it.toModel() } }

    fun getAllReports(): Flow<List<Report>> =
        reportDao.getAllReports().map { list -> list.map { it.toModel() } }
}
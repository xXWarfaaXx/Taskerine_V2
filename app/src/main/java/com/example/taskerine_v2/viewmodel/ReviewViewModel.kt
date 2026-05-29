package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerine_v2.data.model.Review
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReviewViewModel(private val repository: TaskerineRepository) : ViewModel() {

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask

    private val _targetUser = MutableStateFlow<User?>(null)
    val targetUser: StateFlow<User?> = _targetUser

    private val _alreadyReviewed = MutableStateFlow(false)
    val alreadyReviewed: StateFlow<Boolean> = _alreadyReviewed

    fun loadTaskData(taskId: String, currentUserId: String?) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            _currentTask.value = task

            if (task == null) return@launch

            val targetUserId = when {
                currentUserId == task.requesterId -> task.acceptedById
                currentUserId == task.acceptedById -> task.requesterId
                else -> null
            }

            if (targetUserId != null) {
                _targetUser.value = repository.getUserById(targetUserId)
                repository.getReviewsForUser(targetUserId).collect { list ->
                    _reviews.value = list
                    _averageRating.value = if (list.isEmpty()) 0.0
                    else list.map { it.rating }.average()
                }
            }

            if (currentUserId != null) {
                _alreadyReviewed.value =
                    repository.getReviewForTask(taskId, currentUserId) != null
            }
        }
    }

    fun submitReview(
        taskId: String,
        taskTitle: String,
        reviewerId: String,
        reviewerName: String,
        targetUserId: String,
        rating: Int,
        comment: String
    ) {
        viewModelScope.launch {
            val existing = repository.getReviewForTask(taskId, reviewerId)
            if (existing != null) return@launch
            val review = Review(
                id = "r${System.currentTimeMillis()}",
                taskId = taskId,
                taskTitle = taskTitle,
                reviewerId = reviewerId,
                reviewerName = reviewerName,
                targetUserId = targetUserId,
                rating = rating,
                comment = comment
            )
            repository.submitReview(review)
            _submitSuccess.value = true
            _alreadyReviewed.value = true
        }
    }

    fun clearSuccess() {
        _submitSuccess.value = false
    }
}
package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.taskerine_v2.data.model.Review
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReviewViewModel : ViewModel() {

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating

    fun loadReviewsForUser(userId: String) {
        _reviews.value = TaskerineRepository.getReviewsForUser(userId)
        _averageRating.value = TaskerineRepository.getAverageRating(userId)
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
        val existing = TaskerineRepository.getReviewForTask(taskId, reviewerId)
        if (existing != null) return // already reviewed

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
        TaskerineRepository.submitReview(review)
        _submitSuccess.value = true
        loadReviewsForUser(targetUserId)
    }

    fun clearSuccess() {
        _submitSuccess.value = false
    }
}


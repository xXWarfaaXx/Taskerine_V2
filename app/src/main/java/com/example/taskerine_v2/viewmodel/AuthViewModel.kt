package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: TaskerineRepository) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUserFlow

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // --- Profile update state ---
    private val _profileUpdateError = MutableStateFlow<String?>(null)
    val profileUpdateError: StateFlow<String?> = _profileUpdateError

    private val _profileUpdateSuccess = MutableStateFlow(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.login(email, password)
            if (user != null) {
                _error.value = null
                onResult(true)
            } else {
                _error.value = "Invalid email or password"
                onResult(false)
            }
        }
    }

    fun register(username: String, email: String, role: Role, onResult: (Boolean) -> Unit) {
        if (username.isBlank() || email.isBlank()) {
            _error.value = "All fields are required"
            onResult(false)
            return
        }
        viewModelScope.launch {
            repository.register(username, email, role)
            _error.value = null
            onResult(true)
        }
    }

    fun logout() = repository.logout()

    /**
     * Updates the current user's username and/or email.
     * Validation and persistence both happen in the repository.
     */
    fun updateProfile(newUsername: String, newEmail: String) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val result = repository.updateUserProfile(userId, newUsername, newEmail)
            result.fold(
                onSuccess = {
                    _profileUpdateError.value = null
                    _profileUpdateSuccess.value = true
                },
                onFailure = { e ->
                    _profileUpdateError.value = e.message ?: "Failed to update profile"
                    _profileUpdateSuccess.value = false
                }
            )
        }
    }

    fun clearProfileUpdateState() {
        _profileUpdateError.value = null
        _profileUpdateSuccess.value = false
    }
}
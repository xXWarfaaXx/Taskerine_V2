package com.example.taskerine_v2.viewmodel


import androidx.lifecycle.ViewModel
import com.taskerine.data.model.Role
import com.taskerine.data.model.User
import com.taskerine.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String): Boolean {
        val user = TaskerineRepository.login(email, password)
        return if (user != null) {
            _currentUser.value = user
            _error.value = null
            true
        } else {
            _error.value = "Invalid email or password"
            false
        }
    }

    fun register(username: String, email: String, role: Role): Boolean {
        if (username.isBlank() || email.isBlank()) {
            _error.value = "All fields are required"
            return false
        }
        val user = TaskerineRepository.register(username, email, role)
        _currentUser.value = user
        _error.value = null
        return true
    }

    fun logout() {
        TaskerineRepository.logout()
        _currentUser.value = null
    }
}
package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerine_v2.data.model.CoinPackage
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoinViewModel(private val repository: TaskerineRepository) : ViewModel() {

    private val _purchaseSuccess = MutableStateFlow<String?>(null)
    val purchaseSuccess: StateFlow<String?> = _purchaseSuccess

    private val _liveCoins = MutableStateFlow(0)
    val liveCoins: StateFlow<Int> = _liveCoins

    fun loadCoins(userId: String) {
        viewModelScope.launch {
            _liveCoins.value = repository.getCoins(userId)
        }
    }

    fun purchasePackage(userId: String, pkg: CoinPackage, quantity: Int = 1) {
        val totalCoins = (pkg.coins + pkg.bonus) * quantity
        viewModelScope.launch {
            repository.addCoins(userId, totalCoins)
            _liveCoins.value = repository.getCoins(userId)
            val label = if (quantity > 1) "x$quantity ${pkg.name}" else pkg.name
            _purchaseSuccess.value = "+$totalCoins coins added! ($label)"
        }
    }

    fun clearMessage() {
        _purchaseSuccess.value = null
    }
}
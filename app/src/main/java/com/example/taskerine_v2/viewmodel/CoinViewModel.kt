package com.example.taskerine_v2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.taskerine_v2.data.model.CoinPackage
import com.example.taskerine_v2.data.repository.TaskerineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CoinViewModel : ViewModel() {

    private val _purchaseSuccess = MutableStateFlow<String?>(null)
    val purchaseSuccess: StateFlow<String?> = _purchaseSuccess

    fun purchasePackage(userId: String, pkg: CoinPackage, quantity: Int = 1) {
        val totalCoins = (pkg.coins + pkg.bonus) * quantity
        TaskerineRepository.addCoins(userId, totalCoins)
        val label = if (quantity > 1) "x$quantity ${pkg.name}" else pkg.name
        _purchaseSuccess.value = "+$totalCoins coins added! ($label)"
    }

    fun clearMessage() {
        _purchaseSuccess.value = null
    }
}
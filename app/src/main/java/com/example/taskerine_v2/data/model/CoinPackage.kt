package com.example.taskerine_v2.data.model

data class CoinPackage(
    val id: String,
    val name: String,
    val coins: Int,
    val price: Double,
    val bonus: Int = 0
)

val coinPackages = listOf(
    CoinPackage("c1", "Singular", 1, 1.00),
    CoinPackage("c2", "Small", 5,5.00 ),
    CoinPackage("c3", "Medium", 10, 10.00),
    CoinPackage("c4", "Large", 50, 50.00)
)
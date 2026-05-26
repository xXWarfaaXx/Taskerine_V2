package com.example.taskerine_v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.data.model.CoinPackage
import com.example.taskerine_v2.data.model.User
import com.example.taskerine_v2.data.model.coinPackages
import com.example.taskerine_v2.data.repository.TaskerineRepository
import com.example.taskerine_v2.viewmodel.CoinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinStoreScreen(
    currentUser: User?,
    coinViewModel: CoinViewModel,
    onBack: () -> Unit
) {
    val purchaseSuccess by coinViewModel.purchaseSuccess.collectAsState()
    val liveCoins = currentUser?.id?.let { TaskerineRepository.getCoins(it) } ?: 0

    // quantity state per package
    val quantities = remember { mutableStateMapOf<String, Int>().apply {
        coinPackages.forEach { put(it.id, 1) }
    }}

    LaunchedEffect(purchaseSuccess) {
        if (purchaseSuccess != null) {
            kotlinx.coroutines.delay(2000)
            coinViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coin Store") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Balance card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Your Balance",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            "🪙 $liveCoins coins",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("👤 ${currentUser?.username}", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            purchaseSuccess?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        "✅ $it",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Choose a Package", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(coinPackages) { pkg ->
                    val qty = quantities[pkg.id] ?: 1
                    CoinPackageCard(
                        pkg = pkg,
                        quantity = qty,
                        onIncrease = { quantities[pkg.id] = qty + 1 },
                        onDecrease = { if (qty > 1) quantities[pkg.id] = qty - 1 },
                        onPurchase = {
                            currentUser?.id?.let {
                                coinViewModel.purchasePackage(it, pkg, qty)
                                quantities[pkg.id] = 1
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CoinPackageCard(
    pkg: CoinPackage,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onPurchase: () -> Unit
) {
    val totalCoins = (pkg.coins + pkg.bonus) * quantity
    val totalPrice = pkg.price * quantity

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "🪙 ${pkg.coins} coins" + if (pkg.bonus > 0) " + ${pkg.bonus} bonus" else "",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (pkg.bonus > 0) {
                        Text(
                            "Per pack: ${pkg.coins + pkg.bonus} coins",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    "£%.2f each".format(pkg.price),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(32.dp),
                        enabled = quantity > 1
                    ) {
                        Text("−", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "$quantity",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    FilledTonalIconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+", fontWeight = FontWeight.Bold)
                    }
                }

                // Total + buy button
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "🪙 $totalCoins coins",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onPurchase,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Buy £%.2f".format(totalPrice), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
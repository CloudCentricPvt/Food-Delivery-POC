package com.cccinfotech.fooddeliverypoc.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun OrderStatusScreen(repository: FirestoreRepository, orderId: String) {
    var status by remember { mutableStateOf("Loading...") }

    LaunchedEffect(orderId) {
        repository.listenOrder(orderId) { data ->
            status = data?.get("status")?.toString() ?: "Unknown"
        }
    }

    Text(text = "Order Status: $status", style = MaterialTheme.typography.headlineMedium)
}

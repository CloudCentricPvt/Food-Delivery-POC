package com.cccinfotech.fooddeliverypoc.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AssignedOrdersScreen(deliveryBoyId: String) {
    val db = FirebaseFirestore.getInstance()
    val orders = remember { mutableStateListOf<DocumentSnapshot>() }

    LaunchedEffect(Unit) {
        db.collection("orders")
            .whereEqualTo("deliveryBoyId", deliveryBoyId)
            .whereEqualTo("status", "Accepted")
            .addSnapshotListener { snapshot, _ ->
                orders.clear()
                if (snapshot != null) {
                    orders.addAll(snapshot.documents)
                }
            }
    }

    LazyColumn {
        items(orders) { doc ->
            val orderId = doc.id
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order: $orderId")
               Button(onClick = {
                    db.collection("orders").document(orderId)
                        .update("status", "OutForDelivery")
                }) {
                    Text("Start Delivery")
                }
            }
        }
    }
}
fun updateDeliveryLocation(orderId: String, lat: Double, lng: Double) {
    val db = FirebaseFirestore.getInstance()
    db.collection("orders").document(orderId)
        .update("deliveryBoyLocation", mapOf("lat" to lat, "lng" to lng))
}


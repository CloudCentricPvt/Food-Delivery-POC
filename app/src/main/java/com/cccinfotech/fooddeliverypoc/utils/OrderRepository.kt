package com.cccinfotech.fooddeliverypoc.utils

import com.google.firebase.firestore.FirebaseFirestore

object OrderRepository {

    private val db = FirebaseFirestore.getInstance()

    fun updateDeliveryLocation(orderId: String, lat: Double, lng: Double) {
        db.collection("orders").document(orderId)
            .update("deliveryBoyLocation", mapOf("lat" to lat, "lng" to lng))
    }

    fun updateOrderStatus(orderId: String, status: String) {
        db.collection("orders").document(orderId)
            .update("status", status)
    }
}

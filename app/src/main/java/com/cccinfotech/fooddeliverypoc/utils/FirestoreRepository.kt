package com.cccinfotech.fooddeliverypoc.utils

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getRestaurants(onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("restaurants").get()
            .addOnSuccessListener { result ->
                val data = result.documents.map { it.data!! }
                onResult(data)
            }
    }

    fun addOrder(order: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.collection("orders").add(order)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun listenOrder(orderId: String, onUpdate: (Map<String, Any>?) -> Unit) {
        db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, _ ->
                onUpdate(snapshot?.data)
            }
    }

    fun updateDeliveryLocation(orderId: String, lat: Double, lng: Double) {
        db.collection("orders").document(orderId)
            .update("deliveryBoyLocation", mapOf("lat" to lat, "lng" to lng))
    }
}

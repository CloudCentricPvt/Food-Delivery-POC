package com.cccinfotech.fooddeliverypoc.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapViewModel(currentOrderId: String) : ViewModel() {

    private val databaseRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("delivery_boy/current_location/$currentOrderId")

    var orderId = mutableStateOf("")
        private set

    var currentLocation = mutableStateOf<LatLng?>(null)
        private set

    var isLoading = mutableStateOf(true)
        private set

    private val _locations = mutableStateOf<List<LatLng>>(emptyList())
    val locations: State<List<LatLng>> = _locations

    init {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("Sanap","$snapshot")
                    val lat = snapshot.child("lat").getValue(Double::class.java)
                    val lng = snapshot.child("lng").getValue(Double::class.java)
                    val fetchOrderId = snapshot.child("orderId").getValue(String::class.java)

                    if (lat != null && lng != null && fetchOrderId != null) {
                        val newLatLng = LatLng(lat, lng)

                        if (_locations.value.isEmpty()) {
                            _locations.value = listOf(newLatLng)
                        } else {
                            _locations.value += newLatLng
                        }

                        currentLocation.value = newLatLng
                        orderId.value = fetchOrderId
                        isLoading.value = false
                    } else {
                        isLoading.value = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Sanape","$error")

                isLoading.value = false
            }
        })
    }
}

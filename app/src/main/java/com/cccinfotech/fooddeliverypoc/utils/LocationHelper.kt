package com.cccinfotech.fooddeliverypoc.utils

import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(onLocation: (Double, Double) -> Unit) {
        try {
            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(0)
                .build()

            fusedLocationClient.getCurrentLocation(locationRequest, null)
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        onLocation(it.latitude, it.longitude)
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

package com.cccinfotech.fooddeliverypoc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


@SuppressLint("MissingPermission")
fun startLocationUpdates(context: Context, onLocation: (Double, Double) -> Unit): LocationCallback {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L // update every 5 seconds
    ).build()

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                onLocation(location.latitude, location.longitude)
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        request,
        callback,
        Looper.getMainLooper()
    )

    return callback // useful if you want to stop updates later
}

fun stopLocationUpdates(context: Context, callback: LocationCallback) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.removeLocationUpdates(callback)
}


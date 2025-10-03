package com.cccinfotech.fooddeliverypoc.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.FirebaseDatabase
import java.util.Date

class LocationUpdater(private val activity: Activity) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    private val database = FirebaseDatabase.getInstance().reference
    private var locationCallback: LocationCallback? = null

    fun startLocationUpdates(userId: String) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000
        ).setMinUpdateIntervalMillis(10_000)
            .build()
        Log.d("Started","Done")

        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationUpdater", "Permission not granted!")
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location: Location in locationResult.locations) {
                    sendLocationToFirebase(userId, location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            activity.mainLooper
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private fun sendLocationToFirebase(userId: String, location: Location) {
        val locationData = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to Date().time
        )

        database.child("users").child(userId).child("location").setValue(locationData)
            .addOnSuccessListener {
                Log.d("Firebase", "Location updated: $locationData")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Failed to update location", it)
            }
    }
}
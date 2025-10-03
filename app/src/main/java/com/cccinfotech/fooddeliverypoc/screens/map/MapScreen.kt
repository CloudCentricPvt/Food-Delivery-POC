package com.cccinfotech.fooddeliverypoc.screens.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.cccinfotech.fooddeliverypoc.utils.LocationUpdater
import com.cccinfotech.fooddeliverypoc.utils.rememberMapViewWithLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MapScreen(navController: NavController) {
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity
    val locationUpdater = LocationUpdater(context)

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            checkGpsAndLoadMap(activity, mapView)
        }
    }

    DisposableEffect(Unit) {
        locationUpdater.startLocationUpdates(userId = "123456")

        onDispose {
            locationUpdater.stopLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission(context)) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            checkGpsAndLoadMap(activity, mapView)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("ViewMapScreen")
                },
                containerColor = Color.Blue
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                AndroidView({ mapView }) { }

            }
        }
    )
}

private fun checkGpsAndLoadMap(activity: Activity, mapView: com.google.android.gms.maps.MapView) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 1000L
    ).build()

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)

    val client: SettingsClient = LocationServices.getSettingsClient(activity)
    val task = client.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        Log.d("GPS", "GPS already enabled ")
        loadCurrentLocation(activity, mapView)
    }

    task.addOnFailureListener { e ->
        if (e is ResolvableApiException) {
            try {
                e.startResolutionForResult(activity, 1001)
            } catch (sendEx: IntentSender.SendIntentException) {
                sendEx.printStackTrace()
            }
        }
    }
}

private fun loadCurrentLocation(activity: Activity, mapView: com.google.android.gms.maps.MapView) {
    CoroutineScope(Dispatchers.Main).launch {
        val map = mapView.awaitMap()
        map.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return@launch

        map.isMyLocationEnabled = true

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    Log.d("MapScreen", "Current location: ${it.latitude}, ${it.longitude}")
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

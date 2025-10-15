package com.cccinfotech.fooddeliverypoc.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.model.sendorder.Orders
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.viewmodel.MapViewModel
import com.cccinfotech.fooddeliverypoc.viewmodelfactory.MapViewModelFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun ViewMapScreen(
    navController: NavController,
) {
    val context = LocalContext.current

    val order = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<Orders>("order")

    val mapViewModel: MapViewModel? = order?.orderId?.let { orderId ->
        viewModel(factory = MapViewModelFactory(orderId))
    }

    if (mapViewModel == null) {
        return
    }
    val locations by mapViewModel.locations
    val firebaseLocation by mapViewModel.currentLocation
    val orderId by mapViewModel.orderId
    val isLoading by mapViewModel.isLoading

    var vehicleIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val apiKey = context.getString(R.string.google_maps_key)

    val testDestination = order.let {
        it.orderLatitude?.let { it1 ->
            it.orderLongitude?.let { it2 ->
                LatLng(
                    it1,
                    it2
                )
            }
        }
    }
    val vehicleMarkerState = remember { MarkerState() }

    // 🔹 Fetch route points once we have firebase + destination
    LaunchedEffect(firebaseLocation, testDestination) {
        Log.d("OrderId","${order.orderId}")
        Log.d("OrderTwo", mapViewModel.orderId.value)
        if (firebaseLocation != null && order.orderId == mapViewModel.orderId.value) {
            routePoints = testDestination?.let {
                getDirections(
                    context,
                    firebaseLocation!!,
                    it,
                    apiKey
                )
            }!!
        }
    }

    // 🔹 Safe initial camera state (fallback position if null)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            firebaseLocation ?: LatLng(28.628454, 77.376945),
            12f
        )
    }

    LaunchedEffect(locations) {
        locations.lastOrNull()?.let { latest ->
            vehicleMarkerState.position = latest
        }
    }

    Scaffold(modifier = Modifier.padding(), content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                CommonUtils().CommonText("${order.customerName}", fontSize = 15)
                Spacer(modifier = Modifier.height(2.dp))
                val itemNames = order.items?.joinToString(", ") { it.productName ?: "" }
                CommonUtils().CommonText("Your order $itemNames running status.", fontSize = 15)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)) {

                if (order.orderId == orderId) {
                    GoogleMap(
                        onMapLoaded = {
                            val scale = (20 * context.resources.displayMetrics.density).toInt()
                            vehicleIcon = bitmapDescriptorFromVector(
                                context = context,
                                vectorResId = R.drawable.bike,
                                width = scale,
                                height = scale
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        locations.firstOrNull()?.let {
                            Marker(state = MarkerState(it), title = "Start Point")
                        }

                        if (vehicleIcon != null) {
                            Marker(
                                state = vehicleMarkerState,
                                icon = vehicleIcon!!,
                                title = "Delivery Vehicle"
                            )
                        }

                        testDestination?.let { MarkerState(it) }?.let {
                            Marker(
                                state = it,
                                title = "Destination Point"
                            )
                        }

                        // ✅ Polyline for delivery path (all Firebase points)
                        if (locations.size > 1) {
                            Polyline(
                                points = locations,
                                color = Color.Green,
                                width = 6f
                            )
                        }

                        // ✅ Polyline for Google Directions route
                        if (routePoints.isNotEmpty()) {
                            Polyline(
                                points = routePoints,
                                color = Color.Blue,
                                width = 8f
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    })

    LaunchedEffect(firebaseLocation) {
        firebaseLocation?.let { latLng ->
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(latLng, 15f)
        }
    }
}

/**
 * Fetch directions safely on IO dispatcher
 */
suspend fun getDirections(
    context: Context,
    origin: LatLng,
    destination: LatLng,
    apiKey: String
): List<LatLng> = withContext(Dispatchers.IO) {
    val url = "https://maps.googleapis.com/maps/api/directions/json?" +
            "origin=${origin.latitude},${origin.longitude}" +
            "&destination=${destination.latitude},${destination.longitude}" +
            "&mode=driving" +
            "&key=$apiKey"

    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        Log.d("Connection", "${connection.responseCode}")
        val data = connection.inputStream.bufferedReader().use { it.readText() }

        val json = JSONObject(data)
        val routes = json.getJSONArray("routes")
        Log.d("Routes", "$routes")
        if (routes.length() > 0) {
            val points = routes.getJSONObject(0)
                .getJSONObject("overview_polyline")
                .getString("points")
            decodePolyline(points)
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}


// Polyline decoder (same as Google sample)
private fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
        lng += dlng

        val latLng = LatLng(lat / 1E5, lng / 1E5)
        poly.add(latLng)
    }

    return poly
}


fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    width: Int = 50,
    height: Int = 50
): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

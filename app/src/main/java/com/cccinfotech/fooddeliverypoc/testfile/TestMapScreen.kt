package com.cccinfotech.fooddeliverypoc.testfile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cccinfotech.fooddeliverypoc.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun TestMap() {

    val context = LocalContext.current
    var vehicleIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val origin = LatLng(28.6289, 77.3707)
    val destination = LatLng(28.4744, 77.5030)

    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val apiKey = context.getString(R.string.google_maps_key)
    val vehicleMarkerState = remember { MarkerState() }
    val cameraPositionState = rememberCameraPositionState()

    var coveredPath by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var remainingPath by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    LaunchedEffect(origin, destination) {
        routePoints = getDirections(
            context,
            destination,
            origin,
            apiKey
        )
    }

    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            animateMarkerAlongPath(vehicleMarkerState, routePoints, durationPerSegment = 2_000L)
        }
    }

    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            for (i in routePoints.indices) {
                vehicleMarkerState.position = routePoints[i]

                coveredPath = routePoints.subList(0, i + 1)
                remainingPath = routePoints.subList(i, routePoints.size)

                delay(2_000)
            }
        }
    }

    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            routePoints.forEach { builder.include(it) }
            val bounds = builder.build()

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    }

    Scaffold(modifier = Modifier.padding(), content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {

                GoogleMap(
                    onMapLoaded = {
                        val scale = (20 * context.resources.displayMetrics.density).toInt()
                        vehicleIcon =
                            bitmapDescriptorFromVector(
                                context = context,
                                vectorResId = R.drawable.bike,
                                width = scale,
                                height = scale
                            )
                    },
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                ) {

                    Marker(state = MarkerState(origin), title = "Start Point")

                    if (vehicleIcon != null) {
                        Marker(
                            state = vehicleMarkerState,
                            icon = vehicleIcon!!,
                            title = "Delivery Vehicle"
                        )
                    }

                    Marker(
                        state = MarkerState(destination),
                        title = "Destination Point"
                    )

                    // ✅ Polyline for Google Directions route
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = Color.Blue,
                            width = 5f
                        )
                    }
                    if (coveredPath.isNotEmpty()) {
                        Polyline(
                            points = coveredPath,
                            color = Color.Green,
                            width = 8f
                        )
                    }

                    // ✅ Remaining path (Blue)
                    if (remainingPath.isNotEmpty()) {
                        Polyline(
                            points = remainingPath,
                            color = Color.Blue,
                            width = 5f
                        )
                    }

                }

            }
        }
    })

}

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

suspend fun animateMarkerAlongPath(
    markerState: MarkerState,
    path: List<LatLng>,
    durationPerSegment: Long = 10_000L
) {
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]

        val steps = 100
        val stepDuration = durationPerSegment / steps

        for (step in 0..steps) {
            val fraction = step / steps.toFloat()
            val newPos = interpolateLatLng(start, end, fraction)
            markerState.position = newPos
            delay(stepDuration)
        }
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

fun interpolateLatLng(start: LatLng, end: LatLng, fraction: Float): LatLng {
    val lat = start.latitude + (end.latitude - start.latitude) * fraction
    val lng = start.longitude + (end.longitude - start.longitude) * fraction
    return LatLng(lat, lng)
}


fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    width: Int = 60,
    height: Int = 60
): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
package com.cccinfotech.fooddeliverypoc.screens.searchplace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cccinfotech.fooddeliverypoc.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@Composable
fun SearchPlaces() {

    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }

    // Initialize Places
    val placesClient: PlacesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(R.string.google_maps_key))
        }
        Places.createClient(context)
    }

    Scaffold(modifier = Modifier.fillMaxSize(), content = {padding->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TextField(
                value = query,
                onValueChange = { text ->
                    query = text
                    if (text.isNotEmpty()) {
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(text)
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                suggestions = response.autocompletePredictions
                            }
                            .addOnFailureListener {
                                suggestions = emptyList()
                            }
                    } else {
                        suggestions = emptyList()
                    }
                },
                label = { Text("Search Address") },
                modifier = Modifier.fillMaxWidth()
            )

            // Show suggestions
            if (suggestions.isNotEmpty()) {
                LazyColumn {
                    items(suggestions) { prediction ->
                        Text(
                            text = prediction.getFullText(null).toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = prediction.getFullText(null).toString()
                                    suggestions = emptyList()
                                    // TODO: handle selected address
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

    })

}

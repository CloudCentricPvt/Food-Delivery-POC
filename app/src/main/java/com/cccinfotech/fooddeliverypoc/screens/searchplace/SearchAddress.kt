package com.cccinfotech.fooddeliverypoc.screens.searchplace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@Composable
fun SearchPlaces(navHostController: NavHostController) {

    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }

    // Initialize Places
    val placesClient: PlacesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(R.string.google_maps_key))
        }
        Places.createClient(context)
    }

    Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
       Box(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
           Button(modifier = Modifier.fillMaxWidth().padding(4.dp),onClick = {
               navHostController.previousBackStackEntry
                   ?.savedStateHandle
                   ?.set("Address", address)

               navHostController.popBackStack()
           }) {CommonUtils().CommonText("Select Address", color = Color.White) }
       }
    }, content = {padding->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                OutlinedTextField(
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
                    maxLines = 1,
                    label = { CommonUtils().CommonText("Search Address") },
                    modifier = Modifier.fillMaxWidth()
                )

            }
            if (suggestions.isNotEmpty()) {
                LazyColumn {
                    items(suggestions) { prediction ->
                        CommonUtils().CommonText(
                            text = prediction.getFullText(null).toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = prediction.getFullText(null).toString()
                                    address=query
                                    suggestions = emptyList()
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    })

}

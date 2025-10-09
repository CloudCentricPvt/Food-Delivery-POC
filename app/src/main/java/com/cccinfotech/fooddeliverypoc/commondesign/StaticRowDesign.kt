package com.cccinfotech.fooddeliverypoc.commondesign

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cccinfotech.fooddeliverypoc.model.product.Product
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils

@Composable
fun CategoryRow(
    allItems: List<Product>,
    onFiltered: (List<Product>) -> Unit
) {
    val categories = listOf("All", "Veg", "Non-Veg", "Snacks")

    var selectedCategory by remember { mutableStateOf("All") }

    // ✅ Call once when composable first loads
    LaunchedEffect(allItems) {
        Log.d("AllItems", "CategoryRow received ${allItems.size} items")
        if (allItems.isNotEmpty()) {
            onFiltered(allItems)
        }
    }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            Button(
                modifier = Modifier
                    .width(120.dp)
                    .height(46.dp)
                    .padding(5.dp),
                onClick = {
                    selectedCategory = category

                    val filteredList = if (category == "All") {
                        allItems
                    } else {
                        allItems.filter { it.category == category }
                    }

                    Log.d("FilteredList", filteredList.toString())

                    onFiltered(filteredList)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCategory == category)
                        Color(0xFF009688)
                    else
                        Color.LightGray
                )
            ) {
                CommonUtils().CommonText(
                    text = category,
                    color = if (selectedCategory == category) Color.White else Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

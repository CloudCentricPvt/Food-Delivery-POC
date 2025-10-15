package com.cccinfotech.fooddeliverypoc.component_design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.model.restaurent.Restaurant
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils

@Composable
fun CommonRestaurantCard(restaurant: Restaurant,onItemClick: () -> Unit) {

    val utils = remember { CommonUtils() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        Card(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth().clickable {
                    onItemClick()
                },
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(restaurant.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                restaurant.name?.let {
                    utils.CommonText(
                        it,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16
                    )
                }
                restaurant.restaurantAddress?.let {
                    utils.CommonText(
                        it,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        maxLine = 1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_menu_book_24),
                        contentDescription = "Order Icon",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {

                            }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    restaurant.category?.let {
                        utils.CommonText(
                            it,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.rating),
                        contentDescription = "Order Icon",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {

                            }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    restaurant.rating?.let {
                        utils.CommonText(
                            it,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
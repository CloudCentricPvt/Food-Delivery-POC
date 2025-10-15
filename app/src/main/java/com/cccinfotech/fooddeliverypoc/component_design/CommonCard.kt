package com.cccinfotech.fooddeliverypoc.component_design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils

@Composable
fun CommonCard(
    price:String,
    title: String,
    subTitle: String,
    imageUrl: String,
    onItemClick: () -> Unit,
    onClick: () -> Unit,
) {

    val utils = remember { CommonUtils() }


    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                onItemClick()
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp),

        ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(9f)) {
                utils.CommonText(title, fontWeight = FontWeight.Medium, fontSize = 16, maxLine = 1)
                utils.CommonText(subTitle, fontSize = 12, fontWeight = FontWeight.Normal, maxLine = 1)
                Row {
                    utils.CommonText("Price ₹ ", fontSize = 12, fontWeight = FontWeight.Normal)
                    utils.CommonText(price, fontSize = 12, fontWeight = FontWeight.Medium)
                }
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Order Icon",
                tint = Color.Black,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        onClick()
                    }
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    CommonCard(
        "",
        title = "Pizza Order",
        subTitle = "This Is my Order",
        imageUrl = "", {}
    ) {}

}

package com.cccinfotech.fooddeliverypoc.commondesign

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils


@Composable
fun CommonCard(
    title: String,
    subTitle: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                model = imageUrl.ifEmpty { R.drawable.food },
                contentDescription = "Sample Image",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(0.dp, Color.White, RectangleShape),
                contentScale = ContentScale.Crop,

                placeholder = painterResource(R.drawable.food),
                error = painterResource(R.drawable.food)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column() {
                CommonUtils().CommonText(
                    title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16,
                )
                CommonUtils().CommonText(
                    subTitle,
                    fontSize = 12,
                    fontWeight = FontWeight.Normal,
                )

            }
            Spacer(modifier = Modifier.weight(1f))
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
        title = "Pizza Order",
        subTitle = "This Is my Order",
        imageUrl = ""
    ) {

    }

}

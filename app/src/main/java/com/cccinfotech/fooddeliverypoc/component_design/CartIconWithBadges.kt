package com.cccinfotech.fooddeliverypoc.component_design

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils

@Composable
fun CartIconWithBadge(
    cartCount: Int,
    navController: NavController?
) {
    BadgedBox(
        modifier = Modifier.padding(end = 8.dp),
        badge = {
            if (cartCount > 0) {
                Badge {
                    CommonUtils().CommonText(
                        text = cartCount.toString(),
                        color = Color.White,
                        fontSize = 10
                    )
                }
            }
        }
    ) {
        IconButton(onClick = {
            navController?.navigate("Payment")
        }) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.Black
            )
        }
    }
}
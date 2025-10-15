package com.cccinfotech.fooddeliverypoc.screens.product

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.cccinfotech.fooddeliverypoc.model.product.Product
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.Poppins
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(navHostController: NavHostController) {

    val product = navHostController
        .previousBackStackEntry
        ?.savedStateHandle
        ?.get<Product>("product")

    var isLoading by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val context= LocalContext.current

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,

                    ) {

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val order = SendOrder(
                                productId = product?.id ?: "",
                                productName = product?.p_name.toString(),
                                productDetails = product?._description.toString(),
                                quantity = 1,
                                orderId = "",
                                currentTime = "",
                                customerName = SharedPrefManager.getString("UserName"),
                                deliveryBoye = "",
                                customerId = SharedPrefManager.getString("UserId"),
                                amount = "${(product?._rate?.toDouble())}",
                                status = "Pending",
                                orderLongitude = 0.0,
                                orderLatitude = 0.0,
                                orderDate = "",
                                orderAddress = "",
                                productImage = product?.image_url
                            )
                            isLoading = true
                            db.collection("CartItems")
                                .add(order)
                                .addOnSuccessListener { _ ->
                                    isLoading = false
                                    Toast.makeText(context,"Item Added successfully",Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(context,"$e",Toast.LENGTH_SHORT).show()
                                }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            CommonUtils().CommonText(
                                "Please wait...",
                                fontWeight = FontWeight.W600,
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CommonUtils().CommonText(
                                    "Add to cart ",
                                    fontWeight = FontWeight.W600,
                                    color = Color.White,
                                    fontSize = 15
                                )
                            }
                        }
                    }
                }
            }
        },
        topBar = {
            TopAppBar(title = {
                CommonUtils().CommonText(
                    "Product Details",
                    fontSize = 17,
                    fontWeight = FontWeight.W500
                )
            },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(15.dp)
            ) {
                if (product != null) {
                    Image(
                        painter = rememberImagePainter(product.image_url),
                        contentDescription = "product.image_url",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                if (product != null) {
                    CommonUtils().CommonText("Product Name :- ${product.p_name} ")
                }
                Spacer(modifier = Modifier.height(5.dp))
                if (product != null) {
                    CommonUtils().CommonText("Amount :- ${product._rate}")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    CommonUtils().CommonText("Description")
                    Spacer(modifier = Modifier.height(1.dp))
                    if (product != null) {
                        CommonUtils().CommonText("${product._description}")
                    }
                }

            }
        })

}
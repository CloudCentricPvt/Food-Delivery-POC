package com.cccinfotech.fooddeliverypoc.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.model.sendorder.Orders
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf<List<Orders>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

    var selectedOrder by remember { mutableStateOf<Orders?>(null) }

    LaunchedEffect(Unit) {
        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    Log.e("Firestore", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

                orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Orders::class.java)?.copy(orderId = doc.id)
                }?.sortedByDescending { order ->
                    val combined = "${order.orderDate} ${order.currentTime}"
                    runCatching { LocalDateTime.parse(combined, formatter) }
                        .getOrNull() ?: LocalDateTime.MIN
                } ?: emptyList()
            }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { CommonUtils().CommonText("Your Orders", fontSize = 14) },
                modifier = Modifier.background(Color.White)
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                val userOrders = orders
                    .filter { it.customerId == SharedPrefManager.getString("UserId") }
                    .sortedBy { parseOrderDateTime(it, dateTimeFormatter = dateTimeFormatter) }

                if (userOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding), content = {
                            Image(
                                painter = painterResource(id = R.drawable.not_items),
                                contentDescription = "My Image",
                                modifier = Modifier.size(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        }, contentAlignment = Alignment.Center
                    )
                } else {
                    LazyColumn(contentPadding = padding) {
                        itemsIndexed(
                            userOrders,
                            key = { index, order ->
                                "${order.orderId ?: "order"}_$index"
                            }
                        ) { _, order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        selectedOrder = order
                                        Log.d("Order", "$selectedOrder")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (order.status.equals("delivered", true)) {
                                        Color.White
                                    } else {
                                        Color(0xFFF5F5F5)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            CommonUtils().CommonText(
                                                "Order No: ${order.orderNumber ?: ""}",
                                                color = Color.Blue,
                                                fontWeight = FontWeight.W700,
                                                fontSize = 17
                                            )
                                            CommonUtils().CommonText(
                                                "OTP: ${order.deliveryTimeOTP ?: ""}",
                                                color = Color.Red,
                                                fontWeight = FontWeight.W500,
                                                fontSize = 15
                                            )
                                        }
                                        CommonUtils().CommonText("Customer: ${order.customerName}")
                                        order.items?.forEach { item ->
                                            Divider(
                                                color = Color.Gray,
                                                thickness = 0.5.dp
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Text Section (fills remaining width)
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = 12.dp)
                                                ) {
                                                    CommonUtils().CommonText("Product: ${item.productName}")
                                                    CommonUtils().CommonText("Quantity: ${item.quantity}")
                                                    CommonUtils().CommonText("Amount: ₹${item.amount}")
                                                    CommonUtils().CommonText("Details: ${item.productDetails}")
                                                }
                                            }
                                        }

                                        CommonUtils().CommonText("Status :${order.status}")
                                        CommonUtils().CommonText("Order Date :${order.orderDate}")
                                        CommonUtils().CommonText("Order Time :${order.currentTime}")
                                        CommonUtils().CommonText("Delivery Boy: ${order.deliveryBoye}")
                                        if (order.status.equals(
                                                "inprogress",
                                                ignoreCase = true
                                            ) || order.status.equals(
                                                "out for delivery",
                                                ignoreCase = true
                                            )
                                        ) {
                                            CommonUtils().CommonText(
                                                modifier = Modifier.clickable {
                                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                                        "order",
                                                        order
                                                    )
                                                    navController.navigate("ViewMapScreen")
                                                },
                                                text = "Show Order Map",
                                                color = Color.Blue,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                    }
                                    if (order.status.equals("delivered", ignoreCase = true)) {
                                        CommonUtils().CommonText(
                                            "Delivered",
                                            color = Color.Red,
                                            fontWeight = FontWeight.W700
                                        )

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedOrder != null) {
        val order = selectedOrder!!
        AlertDialog(
            onDismissRequest = { selectedOrder = null },
            title = { CommonUtils().CommonText("Order Details") },
            text = {
                Column {
                    CommonUtils().CommonText("#OrderID: ${order.orderId}")
                    CommonUtils().CommonText("Customer: ${order.customerName}")
                    CommonUtils().CommonText("Order Date :${order.orderDate}")
                    CommonUtils().CommonText("Order Time :${order.currentTime}")
                    CommonUtils().CommonText("Status: ${order.status}")
                }
            },
            confirmButton = {
            },
            dismissButton = {
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseOrderDateTime(order: Orders, dateTimeFormatter: DateTimeFormatter): LocalDateTime {
    val dateStr = order.orderDate?.takeIf { it.isNotBlank() } ?: "01-01-1970"
    val timeStr = order.currentTime?.takeIf { it.isNotBlank() } ?: "00:00"

    return try {
        LocalDateTime.parse("$dateStr $timeStr", dateTimeFormatter)
    } catch (e: Exception) {
        // fallback if format is invalid
        LocalDateTime.of(1970, 1, 1, 0, 0)
    }
}


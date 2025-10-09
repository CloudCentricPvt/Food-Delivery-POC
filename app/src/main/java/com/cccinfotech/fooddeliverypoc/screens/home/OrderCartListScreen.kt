package com.cccinfotech.fooddeliverypoc.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cccinfotech.fooddeliverypoc.model.sendorder.Orders
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderCartListScreen(navHostController: NavHostController) {

    val db = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf<List<SendOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

    var selectedOrder by remember { mutableStateOf<SendOrder?>(null) }

    LaunchedEffect(Unit) {
        db.collection("CartItems")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    isLoading = false
                    Log.e("Fire-store", "Listen failed", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    orders = snapshot.toObjects(SendOrder::class.java)
                    isLoading = false
                }
            }
    }
    Scaffold(
        topBar = { TopAppBar(title = { CommonUtils().CommonText("Orders", fontSize = 18) }) }
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
                    .sortedBy { parseOrderDateTimeCart(it, dateTimeFormatter = dateTimeFormatter) }


                LazyColumn(contentPadding = padding) {
                    itemsIndexed(
                        userOrders
                            .filter {
                                it.status.equals(
                                    "inprogress",
                                    ignoreCase = true
                                ) || it.status.equals("Pending", ignoreCase = true)
                            }
                            .sortedBy { parseOrderDateTimeCart(it, dateTimeFormatter) },
                        key = { index, order ->
                            if (order.orderId.isNullOrBlank()) "order_$index" else order.orderId
                        }
                    ) { _, order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { selectedOrder = order },
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
                                    CommonUtils().CommonText("Customer: ${order.customerName}")
                                    CommonUtils().CommonText("Product: ${order.productName}")
                                    CommonUtils().CommonText("Quantity: ${order.quantity}")
                                    CommonUtils().CommonText("Amount: ₹${order.amount}")
                                    CommonUtils().CommonText("Status :${order.status}")
                                    CommonUtils().CommonText("Order Date :${order.orderDate}")
                                    CommonUtils().CommonText("Order Time :${order.currentTime}")
                                    CommonUtils().CommonText("Delivery Boy: ${order.deliveryBoye}")
                                    if (order.status.equals("inprogress", ignoreCase = true)) {
                                        CommonUtils().CommonText(
                                            modifier = Modifier.clickable {
                                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                                    "order",
                                                    order
                                                )
                                                navHostController.navigate("ViewMapScreen")
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

    if (selectedOrder != null) {
        val order = selectedOrder!!
        AlertDialog(
            onDismissRequest = { selectedOrder = null },
            title = { CommonUtils().CommonText("Order Details") },
            text = {
                Column {
                    CommonUtils().CommonText("#OrderID: ${order.orderId}")
                    CommonUtils().CommonText("Customer: ${order.customerName}")
                    CommonUtils().CommonText("Product: ${order.productName}")
                    CommonUtils().CommonText("Quantity: ${order.quantity}")
                    CommonUtils().CommonText("Amount: ₹${order.amount}")
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
fun parseOrderDateTimeCart(order: SendOrder, dateTimeFormatter: DateTimeFormatter): LocalDateTime {
    val dateStr = order.orderDate?.takeIf { it.isNotBlank() } ?: "01-01-1970"
    val timeStr = order.currentTime?.takeIf { it.isNotBlank() } ?: "00:00"

    return try {
        LocalDateTime.parse("$dateStr $timeStr", dateTimeFormatter)
    } catch (e: Exception) {
        // fallback if format is invalid
        LocalDateTime.of(1970, 1, 1, 0, 0)
    }
}

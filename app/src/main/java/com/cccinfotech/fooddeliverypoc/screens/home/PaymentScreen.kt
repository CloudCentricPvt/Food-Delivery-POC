package com.cccinfotech.fooddeliverypoc.screens.home

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.broadcast.NetworkReceiver
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.services.MyForegroundService
import com.cccinfotech.fooddeliverypoc.ui.theme.FoodDeliveryPOCTheme
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.Poppins
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch

class CartScreen : ComponentActivity(), PaymentResultListener {

    private lateinit var networkReceiver: NetworkReceiver

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodDeliveryPOCTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PaymentScreen(navController)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        networkReceiver = NetworkReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkReceiver)
    }

    override fun onPaymentSuccess(p0: String?) {

    }

    override fun onPaymentError(p0: Int, p1: String?) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PaymentScreen(navController: NavController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var cartItems by remember { mutableStateOf<List<SendOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isButtonLoading by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        db.collection("CartItems")
            .addSnapshotListener { snapshot, exception ->
                isLoading = false
                if (exception != null) {
                    Log.e("CartScreen", "Listen failed", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    cartItems = snapshot.documents.map { doc ->
                        val order = doc.toObject(SendOrder::class.java)
                        // attach Firestore docId to your model
                        order?.copy(orderId = doc.id) ?: SendOrder(orderId = doc.id)
                    }
                }
            }
    }

    fun clearCart(db: FirebaseFirestore) {
        val batch = db.batch()

        db.collection("CartItems")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d("CartScreen", "Cart cleared successfully")
                        // also clear local state
                        cartItems = emptyList()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CartScreen", "Error clearing cart", e)
                    }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                CommonUtils().CommonText(
                    "Cart Items",
                    fontSize = 16,
                    fontWeight = FontWeight.W500
                )
            })
        },
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
                    val totalAmount = cartItems.sumOf { order ->
                        order.amount
                            ?.replace("₹", "")
                            ?.replace(",", "")
                            ?.trim()
                            ?.toDoubleOrNull()
                            ?: 0.0
                    }
                    CommonUtils().CommonText("Total: ₹$totalAmount", fontWeight = FontWeight.Bold)

                    Button(onClick = {
                        try {

                            if (cartItems.isEmpty()) {
                                return@Button
                            } else {
                                isButtonLoading = true

                                placeOrder(context, db, cartItems) { _, message, _ ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    cartItems = emptyList()
                                    clearCart(db)
                                    isButtonLoading = false
                                }

                            }


                        } catch (e: Exception) {
                            isButtonLoading = false
                            e.printStackTrace()
                        }
                    }, enabled = !isButtonLoading) {

                        if (isButtonLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Text(
                                "Please wait...", fontFamily = Poppins,
                                fontWeight = FontWeight.W600,
                            )
                        } else {
                            CommonUtils().CommonText("Place Order", color = Color.White)

                        }

                    }
                }
            }
        },
        content = { padding ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (cartItems.isEmpty()) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(padding), content = {
                        Image(
                            painter = painterResource(id = R.drawable.not_items),
                            contentDescription = "My Image",
                            modifier = Modifier.size(150.dp),
                            contentScale = ContentScale.Crop
                        )
                    }, contentAlignment = Alignment.Center
                    )
                } else {
                    LazyColumn(contentPadding = padding) {
                        itemsIndexed(
                            cartItems
                                .filter {
                                    it.status.equals(
                                        "inprogress",
                                        ignoreCase = true
                                    ) || it.status.equals("Pending", ignoreCase = true)
                                },
                            key = { index, order ->
                                if (order.orderId.isNullOrBlank()) "order_$index" else order.orderId
                            }
                        ) { _, order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(1.dp),
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            CommonUtils().CommonText("Product: ${order.productName}")
                                            CommonUtils().CommonText("Quantity: ${order.quantity}")
                                            CommonUtils().CommonText("Amount: ₹${order.amount}")
                                        }

                                        // Remove button
                                        IconButton(onClick = {
                                            order.orderId?.let { id ->
                                                db.collection("CartItems").document(id)
                                                    .delete()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Item removed",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to remove",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Item",
                                                tint = Color.Red
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
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun placeOrder(
    context: Context,
    db: FirebaseFirestore,
    orders: List<SendOrder>,
    onComplete: (Boolean, String, String?) -> Unit
) {
    val itemsList = orders.map { order ->
        mapOf(
            "productName" to order.productName,
            "quantity" to order.quantity,
            "amount" to order.amount
        )
    }

    val orderData = hashMapOf(
        "orderId" to orders.firstOrNull()?.orderId,
        "items" to itemsList,
        "currentTime" to orders.firstOrNull()?.currentTime,
        "status" to "Pending",
        "customerName" to orders.firstOrNull()?.customerName,
        "orderAddress" to orders.firstOrNull()?.orderAddress,
        "orderLatitude" to orders.firstOrNull()?.orderLatitude,
        "orderLongitude" to orders.firstOrNull()?.orderLongitude,
        "orderDate" to orders.firstOrNull()?.orderDate
    )

    db.collection("orders")
        .add(orderData)
        .addOnSuccessListener { documentRef ->
            val orderId = documentRef.id
            onComplete(true, "Order placed successfully", orderId)

            orders.forEach {
                val serviceIntent = Intent(context, MyForegroundService::class.java).apply {
                    action = MyForegroundService.Actions.START.toString()
                    putExtra("orderId", orderId)
                    putExtra("ProductName", it.productName)
                    putExtra("Status", "Pending")

                }
                context.startForegroundService(serviceIntent)
            }
        }
        .addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to place order", null)
        }
}

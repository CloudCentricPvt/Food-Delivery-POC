package com.cccinfotech.fooddeliverypoc.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.broadcast.NetworkReceiver
import com.cccinfotech.fooddeliverypoc.constant.CommonUtil
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.services.MyForegroundService
import com.cccinfotech.fooddeliverypoc.ui.theme.FoodDeliveryPOCTheme
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.Poppins
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PaymentScreen(navController: NavController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var cartItems by remember { mutableStateOf<List<SendOrder>>(emptyList()) }
    var selectedList by remember { mutableStateOf<List<SendOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isButtonLoading by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coroutineScope = rememberCoroutineScope()
    var showGpsDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentAddress by remember { mutableStateOf<String?>("") }
    var sendAddress by remember { mutableStateOf<String?>("") }
    var lat by remember { mutableDoubleStateOf(0.0) }
    var long by remember { mutableDoubleStateOf(0.0) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var productList by remember { mutableStateOf<List<String>>(emptyList()) }


    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")


    LaunchedEffect(Unit) {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            showGpsDialog = true
        } else if (!locationPermissionState.status.isGranted) {
            showPermissionDialog = true
        } else {
            // Fetch location directly
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@LaunchedEffect
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    lat = it.latitude
                    long = it.longitude
                    currentAddress = CommonUtils().getAddressFromLatLng(context, lat, long)

                }
            }
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val address = savedStateHandle?.getLiveData<String>("Address")?.value


    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { CommonUtils().CommonText("Enable GPS") },
            text = { CommonUtils().CommonText("Your GPS is turned off. Please enable it to continue.") },
            confirmButton = {
                TextButton(onClick = {
                    showGpsDialog = false
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }) {
                    CommonUtils().CommonText("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) {
                    CommonUtils().CommonText("Cancel")
                }
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { CommonUtils().CommonText("Permission Required") },
            text = { CommonUtils().CommonText("We need location permission to continue.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    locationPermissionState.launchPermissionRequest()
                }) {
                    CommonUtils().CommonText("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    CommonUtils().CommonText("Cancel")
                }
            }
        )
    }


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
                        order?.copy(orderId = doc.id) ?: SendOrder(orderId = doc.id)
                    }
                }
            }
    }

    fun clearCart(db: FirebaseFirestore, cartList: List<SendOrder>) {
        val batch = db.batch()

        cartList.filter { it.selectedItem == true }.forEach { order ->
            // Make sure you have the document ID
            val docRef = db.collection("CartItems").document(order.orderId ?: "")
            batch.delete(docRef)
        }

        batch.commit()
            .addOnSuccessListener {
                // Update local state if needed
                cartItems = cartItems.filter { it.selectedItem != true }

                Log.d("CartScreen", "Selected items cleared successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CartScreen", "Error clearing selected items", e)
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
        snackbarHost = {
            Box(modifier = Modifier.fillMaxSize()) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 170.dp, start = 16.dp, end = 16.dp)
                ) { data ->
                    val backgroundColor =
                        if (CommonUtil.currentSnackbarSuccess.value)
                            Color(context.getColor(R.color.success))
                        else
                            Color(context.getColor(R.color.failure))

                    Snackbar(
                        containerColor = backgroundColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = data.visuals.message,
                            fontFamily = Poppins
                        )
                    }
                }
            }
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
                    val userOrders = selectedList
                        .filter { it.customerId == SharedPrefManager.getString("UserId") }
                        .sortedBy {
                            parseOrderDateTimeCart(
                                it,
                                dateTimeFormatter = dateTimeFormatter
                            )
                        }

                    val totalAmount = userOrders.sumOf { order ->
                        order.amount
                            ?.replace("₹", "")
                            ?.replace(",", "")
                            ?.trim()
                            ?.toDoubleOrNull()
                            ?: 0.0
                    }
                    CommonUtils().CommonText("Total: ₹$totalAmount", fontWeight = FontWeight.Bold)

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedList.isNotEmpty())
                                Color(0xFF009688)
                            else
                                Color.LightGray
                        ), onClick = {
                            try {
                                if (cartItems.isEmpty()) {
                                    return@Button
                                } else {
                                    isButtonLoading = true
                                    sendAddress?.let {
                                        if (lat != 0.0 && long != 0.0) {
                                            if (selectedList.isNotEmpty()) {
                                                placeOrder(
                                                    lat, long,
                                                    context,
                                                    db,
                                                    selectedList,
                                                    it
                                                ) { _, message, _, list ->
                                                    CommonUtils().showSnackbar(
                                                        message,
                                                        true,
                                                        snackbarHostState,
                                                        coroutineScope
                                                    )
                                                    productList = list
                                                    showDialog = true
                                                    clearCart(db, selectedList)
                                                    selectedList = emptyList()
                                                    isButtonLoading = false
                                                }
                                            } else {
                                                isButtonLoading = false
                                                CommonUtils().showSnackbar(
                                                    "Select At least one item",
                                                    false,
                                                    snackbarHostState,
                                                    coroutineScope
                                                )
                                            }
                                        } else {
                                            isButtonLoading = false
                                            CommonUtils().showSnackbar(
                                                "Check Your Location",
                                                false,
                                                snackbarHostState,
                                                coroutineScope
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                isButtonLoading = false
                                e.printStackTrace()
                            }
                        }, enabled = !isButtonLoading
                    ) {

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
            if (showDialog) {
                CommonUtil.OrderCompletedDialog(productList) {
                    showDialog = false
                    navController.navigateUp()
                }
            }
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
                val userOrders = cartItems
                    .filter { it.customerId == SharedPrefManager.getString("UserId") }
                    .sortedBy { parseOrderDateTimeCart(it, dateTimeFormatter = dateTimeFormatter) }

                if (userOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding), content = {
                            Image(
                                painter = painterResource(id = R.drawable.not_items),
                                contentDescription = "My Image",
                                modifier = Modifier.size(250.dp),
                                contentScale = ContentScale.Crop
                            )
                        }, contentAlignment = Alignment.Center
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {

                            val displayAddress =
                                if (!address.isNullOrBlank()) address else currentAddress
                            sendAddress = displayAddress

                            if (displayAddress != null) {
                                CommonUtils().CommonText(
                                    displayAddress,
                                    color = Color.Black,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CommonUtils().CommonText(
                                "Order for diffrent address",
                                color = Color.Blue,
                                fontSize = 14,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    navController.navigate("Place")
                                })
                        }
                        LazyColumn {
                            itemsIndexed(
                                userOrders
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
                                        containerColor = if (order.status.equals(
                                                "delivered",
                                                true
                                            )
                                        ) {
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

                                            Column {
                                                var checkBoxState by remember {
                                                    mutableStateOf(
                                                        order.selectedItem ?: false
                                                    )
                                                }

                                                Checkbox(
                                                    checked = checkBoxState,
                                                    onCheckedChange = { checked ->
                                                        checkBoxState = checked
                                                        order.selectedItem = checked
                                                        selectedList =
                                                            cartItems.filter { it.selectedItem == true }
                                                        Log.d("SelectedItem", "$selectedList")
                                                    }
                                                )
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
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun placeOrder(
    lat: Double?,
    long: Double,
    context: Context,
    db: FirebaseFirestore,
    orders: List<SendOrder>,
    orderAddress: String,
    onComplete: (Boolean, String, String?, ArrayList<String>) -> Unit
) {

    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val currentTime: String = sdf.format(Date())
    val currentDate: String = date.format(Date())
    val orderNumber = CommonUtils().generateOrderNumber()

    val itemsList = orders.map { order ->
        mapOf(
            "productName" to order.productName,
            "quantity" to order.quantity,
            "amount" to order.amount,
            "productImage" to order.productImage,
            "productDetails" to order.productDetails
        )
    }
    val orderId = db.collection("orders").document().id
    val orderData = hashMapOf(
        "orderId" to orderId,
        "items" to itemsList,
        "currentTime" to currentTime,
        "status" to "Pending",
        "customerName" to orders.firstOrNull()?.customerName,
        "orderAddress" to orderAddress,
        "orderLatitude" to lat,
        "orderLongitude" to long,
        "orderDate" to currentDate,
        "orderNumber" to orderNumber,
        "customerId" to SharedPrefManager.getString("UserId")
    )

    db.collection("orders").document(orderId)
        .set(orderData)
        .addOnSuccessListener {
            val productNames = ArrayList(orders.map { it.productName })
            val serviceIntent = Intent(context, MyForegroundService::class.java).apply {
                action = MyForegroundService.Actions.START.toString()
                putExtra("orderId", orderId)
                putStringArrayListExtra("ProductName", productNames)
                putExtra("Status", "Pending")
            }
            context.startForegroundService(serviceIntent)
            onComplete(true, "Order placed successfully", orderId, productNames)
        }
        .addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to place order", null, arrayListOf())
        }
}

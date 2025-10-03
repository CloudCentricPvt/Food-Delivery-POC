package com.cccinfotech.fooddeliverypoc.screens.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.commondesign.CommonCard
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.model.user.User
import com.cccinfotech.fooddeliverypoc.model.product.Product
import com.cccinfotech.fooddeliverypoc.screens.auth.BiometricAuthScreen
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavController?) {

    val context = LocalContext.current
    val activity = context.findActivity()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<Product?>(null) }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    var showGpsDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var cartCount by remember { mutableIntStateOf(0) }
    var currentAddress by remember { mutableStateOf("") }


    val db = FirebaseFirestore.getInstance()
    var list by remember { mutableStateOf<List<Product>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableDoubleStateOf(0.0) }
    var long by remember { mutableDoubleStateOf(0.0) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                    Log.d("LatLong", "$lat , $long")
                }
            }
        }

        getProduct(db) { productList ->
            list = productList
        }

        getUserData(db, SharedPrefManager.getString("UserId")) { user ->
            name = user.name.toString()
            email = user.email.toString()
            phone = user.phone.toString()
        }
    }

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

    CommonUtils().LogoutDialog(
        showDialog = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        onConfirm = {
            showLogoutDialog = false
            scope.launch { drawerState.close() }
            FirebaseAuth.getInstance().signOut()
            SharedPrefManager.clear()
            navController?.navigate("Auth") { popUpTo(0) }
        }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(250.dp)) {
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .padding(5.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Welcome $name",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = email,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = phone,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Divider()

                NavigationDrawerItem(
                    label = {
                        Text(
                            "Home", fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color.Black
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Text(
                            "See Order", fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()

                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Order",
                            tint = Color.Black
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController?.navigate("Order")
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Text(
                            "Profile", fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PersonPin,
                            contentDescription = "Person",
                            tint = Color.Black
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Text(
                            "Test", fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map",
                            tint = Color.Black
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { navController?.navigate("Test") }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    NavigationDrawerItem(
                        label = {
                            Text(
                                "LogOut", fontFamily = Poppins,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color.Black
                            )
                        },
                        selected = false,
                        onClick = {
                            showLogoutDialog = true
                        })
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
            },
            topBar = {
                TopAppBar(
                    title = {
                        CommonUtils().CommonText(
                            text = "Welcome $name",
                            fontWeight = FontWeight.Normal,
                            fontSize = 18
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {

                        BadgedBox(modifier = Modifier.padding(3.dp),
                            badge = {
                                if (cartCount > 0) {
                                    Badge {
                                        CommonUtils().CommonText(
                                            text = cartCount.toString(),
                                            color = Color.White
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
                )
            },
            snackbarHost = {
                Box(modifier = Modifier.fillMaxSize()) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                    ) { data ->
                        val backgroundColor =
                            if (data.visuals.message.contains("success", ignoreCase = true))
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
            content = { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(list.size) { index ->
                        val product = list[index]
                        CommonCard(
                            title = product.p_name.toString(),
                            subTitle = product._description.toString(),
                            imageUrl = product.image_url.toString(),
                        ) {
                            selectedItem = list[index]
                            showBottomSheet = true
                        }
                    }
                }

                if (showBottomSheet) {
                    selectedItem?.let {
                        Log.d("Lat", "$lat $long")
                        currentAddress =
                            CommonUtils().getAddressFromLatLng(context, lat, long).toString()

                        if (activity != null) {
                            SimpleBottomSheet(
                                context = context,
                                navController,
                                db = db,
                                snackbarHostState = snackbarHostState,
                                coroutineScope = coroutineScope,
                                onDismiss = { showBottomSheet = false },
                                name = name,
                                item = it, lat, activity, long, currentAddress,
                                onOrderPlaced = { addedCount ->
                                    cartCount += addedCount
                                }
                            )
                        }
                    }
                }

            }
        )
    }
}


fun getUserData(db: FirebaseFirestore, userId: String, onResult: (User) -> Unit) {
    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val name = document.getString("name")
                val email = document.getString("email")
                val phone = document.getString("phone")
                onResult(User(name, email, phone))

                Log.d("FirestoreUser", "Name: $name, Email: $email, Phone: $phone")
            } else {
                Log.d("FirestoreUser", "No such document")
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreUser", "Error fetching user", e)
        }

}

fun getProduct(db: FirebaseFirestore, onResult: (List<Product>) -> Unit) {
    db.collection("product")
        .addSnapshotListener { result, _ ->
            val productList = mutableListOf<Product>()
            if (result != null) {
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    val id = document.id
                    productList.add(product.copy(id = id))
                }
            }
            onResult(productList)
        }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleBottomSheet(
    context: Context,
    navController: NavController?,
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
    name: String,
    item: Product,
    latitude: Double,
    activity: Activity,
    longitude: Double,
    orderAddress: String,
    onOrderPlaced: (Int) -> Unit

) {

    var isLoading by remember { mutableStateOf(false) }
    var count by remember { mutableStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight()
        ) {
            Image(
                painter = rememberImagePainter(item.image_url),
                contentDescription = item.p_name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(.8f)
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
            ) {
                Column {
                    CommonUtils().CommonText(
                        "Hello $name",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16,
                    )
                    Spacer(Modifier.height(5.dp))
                    CommonUtils().CommonText(
                        "Your Order is ${item.p_name}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14,
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row {
                    CommonUtils().CommonText("Add Quantity")
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        modifier = Modifier
                            .clickable {
                                if (count <= 1) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Your Quantity is One")
                                    }
                                } else {
                                    count--
                                }
                            }
                            .size(25.dp),
                        painter = painterResource(id = R.drawable.minus),
                        contentDescription = "Minus Icon",
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    CommonUtils().CommonText("$count", fontWeight = FontWeight.W600, fontSize = 16)
                    Spacer(modifier = Modifier.width(20.dp))

                    Icon(
                        modifier = Modifier
                            .clickable {
                                count++
                            }
                            .size(25.dp),
                        painter = painterResource(id = R.drawable.plus_icon),
                        contentDescription = "Plus Icon",
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))

                CommonUtils().CommonText(
                    "$count Items in cart",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13
                )
                Spacer(modifier = Modifier.height(5.dp))
                CommonUtils().CommonText("Your Order Address is : $orderAddress", fontSize = 14)
                Spacer(modifier = Modifier.height(5.dp))
                CommonUtils().CommonText(
                    "Edit Address",
                    color = Color.Blue,
                    fontSize = 14,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        navController?.navigate("Place")
                    })

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val currentTime: String = sdf.format(Date())
                            val currentDate: String = date.format(Date())

                            if (latitude != 0.0 && longitude != 0.0) {
                                val order = SendOrder(
                                    productId = item.id ?: "",
                                    productName = item.p_name.toString(),
                                    quantity = count, orderId = "",
                                    currentTime = currentTime,
                                    customerName = name,
                                    deliveryBoye = "",
                                    customerId = SharedPrefManager.getString("UserId"),
                                    amount = "${(item._rate?.toDouble())?.times(count)}",
                                    status = "Pending",
                                    orderLongitude = longitude,
                                    orderLatitude = latitude,
                                    orderDate = currentDate, orderAddress = orderAddress
                                )
                                isLoading = true
                                db.collection("CartItems")
                                    .add(order)
                                    .addOnSuccessListener { _ ->
                                        coroutineScope.launch {
                                            isLoading = false
                                            snackbarHostState.showSnackbar("Order is Added in the cart")
                                        }
                                        //navController?.navigate("Payment")
                                        onDismiss()
                                    }
                                    .addOnFailureListener { e ->
                                        coroutineScope.launch {
                                            isLoading = false
                                            snackbarHostState.showSnackbar("Something Went Wrong")
                                            onDismiss()
                                        }

                                    }


//                                placeOrder(context, db, order) { _, message, _ ->
//                                    coroutineScope.launch {
//                                        isLoading = false
//                                        snackbarHostState.showSnackbar(message)
//                                    }
//                                    onOrderPlaced(count)
//                                    onDismiss()
//                                }
                            } else {
//                                coroutineScope.launch {
//                                    isLoading = false
//                                    snackbarHostState.showSnackbar("Please Check Your Location")
//                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
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
                        Text(
                            "Please wait...", fontFamily = Poppins,
                            fontWeight = FontWeight.W600,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CommonUtils().CommonText(
                                "Add to cart ",
                                fontWeight = FontWeight.W600,
                                color = Color.White,
                                fontSize = 14
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    FoodDeliveryPOCTheme {
        val navController = rememberNavController()
        HomeScreen(navController)
    }
}


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}


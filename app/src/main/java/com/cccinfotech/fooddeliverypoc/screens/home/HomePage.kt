package com.cccinfotech.fooddeliverypoc.screens.home

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.component_design.AutoSlidingBannerSlider
import com.cccinfotech.fooddeliverypoc.component_design.CartIconWithBadge
import com.cccinfotech.fooddeliverypoc.component_design.CategoryRow
import com.cccinfotech.fooddeliverypoc.component_design.CommonRestaurantCard
import com.cccinfotech.fooddeliverypoc.constant.CommonUtil
import com.cccinfotech.fooddeliverypoc.firebaseservices.FirebaseService
import com.cccinfotech.fooddeliverypoc.model.banner.Banner
import com.cccinfotech.fooddeliverypoc.model.product.Product
import com.cccinfotech.fooddeliverypoc.model.restaurent.Restaurant
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.model.user.User
import com.cccinfotech.fooddeliverypoc.repository.BannerRepository
import com.cccinfotech.fooddeliverypoc.sealed.Resource
import com.cccinfotech.fooddeliverypoc.ui.theme.FoodDeliveryPOCTheme
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.cccinfotech.fooddeliverypoc.viewmodel.BannerViewModel
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavController?) {

    val context = LocalContext.current
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
    var list by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
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

    var selectedCategory by remember { mutableStateOf("All") }
    var filteredList by remember { mutableStateOf(list) }

    val firebaseService = FirebaseService()
    val viewModel: BannerViewModel = remember {
        BannerViewModel(BannerRepository(firebaseService))
    }
    val state by viewModel.bannerState.collectAsState()
    val configuration = LocalConfiguration.current

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isConnected = intent?.getBooleanExtra("isConnected", true) ?: true

                coroutineScope.launch {
                    if (!isConnected) {
                        CommonUtil.showSnackbarForInternet(
                            "Internet is OFF",
                            false,
                            snackbarHostState,
                            coroutineScope
                        )
                    } else {
                        CommonUtil.showSnackbar(
                            "Internet is ON",
                            true,
                            snackbarHostState,
                            coroutineScope
                        )
                    }
                }
            }
        }

        val filter = IntentFilter("NETWORK_STATUS_CHANGED")
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadOffers()
    }

    LaunchedEffect(Unit) {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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

        getRestaurents(db) { productList ->
            list = productList
        }

        getUserData(db, SharedPrefManager.getString("UserId")) { user ->
            name = user.name.toString()
            email = user.email.toString()
            phone = user.phone.toString()
            SharedPrefManager.putString("UserName", name)
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
            SharedPrefManager.remove("UserId")
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
                        CommonUtils().CommonText(
                            text = "Welcome $name",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15,
                        )
                        CommonUtils().CommonText(
                            text = email,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15,
                        )
                        CommonUtils().CommonText(
                            text = phone,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15,
                        )
                    }
                }
                Divider()

                NavigationDrawerItem(
                    label = {
                        CommonUtils().CommonText(
                            "Home",
                            fontWeight = FontWeight.Normal,
                            fontSize = 12
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
                        CommonUtils().CommonText(
                            "See Order",
                            fontWeight = FontWeight.Normal,
                            fontSize = 12,
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
                        CommonUtils().CommonText(
                            "Profile",
                            fontWeight = FontWeight.Normal,
                            fontSize = 12
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
                        scope.launch {
                            drawerState.close()
                            navController?.navigate("HomePageNew")
                        }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        CommonUtils().CommonText(
                            "Test",
                            fontWeight = FontWeight.Normal,
                            fontSize = 12
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
                            CommonUtils().CommonText(
                                "LogOut",
                                fontWeight = FontWeight.Normal,
                                fontSize = 12
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
                            text = "Hello $name",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.hamburger_icon),
                                contentDescription = "Menu",
                            )
                        }
                    },
                    actions = {
                        CartIconWithBadge(cartCount = cartCount, navController = navController)
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
                            if (CommonUtil.currentSnackbarSuccess.value)
                                Color(context.getColor(R.color.success))
                            else
                                Color(context.getColor(R.color.failure))

                        Snackbar(
                            containerColor = backgroundColor,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            CommonUtils().CommonText(
                                text = data.visuals.message, color = Color.White
                            )
                        }
                    }
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (state) {
                        is Resource.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(20.dp)) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        is Resource.Success -> {
                            val banners = (state as Resource.Success<List<Banner>>).data
                            if (banners.isNotEmpty()) {
                                AutoSlidingBannerSlider(banners)
                            }
                        }

                        is Resource.Error -> {
                            CommonUtils().CommonText(
                                text = "Error loading banners: ${(state as Resource.Error).message}",
                                color = Color.Red,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    CategoryRow(
                        allItems = list,
                        onFiltered = { filtered ->
                            filteredList = filtered
                        }
                    )

                    CommonUtils().CommonText(
                        "Restaurants",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18,
                        modifier = Modifier.padding(start = 16.dp) // left margin of 16dp
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp),
                    ) {
                        items(
                            items = filteredList,
                            key = { it.restaurantId ?: it.hashCode() }
                        ) { restaurant ->
                            CommonRestaurantCard(restaurant){
                                navController?.currentBackStackEntry?.savedStateHandle?.set(
                                    "order",
                                    restaurant
                                )
                                navController?.navigate("ProductList")
                            }
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

fun getRestaurents(db: FirebaseFirestore, onResult: (List<Restaurant>) -> Unit) {
    db.collection("restaurent")
        .addSnapshotListener { result, _ ->
            val productList = mutableListOf<Restaurant>()
            if (result != null) {
                for (document in result) {
                    val product = document.toObject(Restaurant::class.java)
                    val id = document.id
                    productList.add(product.copy(restaurantId = id))
                }
            }
            onResult(productList)
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
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}




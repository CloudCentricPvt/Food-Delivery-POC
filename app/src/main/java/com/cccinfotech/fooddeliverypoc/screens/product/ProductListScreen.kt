package com.cccinfotech.fooddeliverypoc.screens.product

import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.component_design.CartIconWithBadge
import com.cccinfotech.fooddeliverypoc.component_design.CommonCard
import com.cccinfotech.fooddeliverypoc.constant.CommonUtil
import com.cccinfotech.fooddeliverypoc.model.product.Product
import com.cccinfotech.fooddeliverypoc.model.restaurent.Restaurant
import com.cccinfotech.fooddeliverypoc.model.sendorder.SendOrder
import com.cccinfotech.fooddeliverypoc.screens.home.findActivity
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navHostController: NavHostController) {

    var list by rememberSaveable { mutableStateOf<List<Product>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<Product?>(null) }
    val db = FirebaseFirestore.getInstance()
    var cartCount by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context.findActivity()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }


    val order = navHostController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<Restaurant>("order")


    LaunchedEffect(Unit) {
        getProduct(db) { productList ->
            list = productList
            isLoading = false
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                CommonUtils().CommonText(
                    "Product List ",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18
                )
            },
            actions = {
                CartIconWithBadge(cartCount = cartCount, navController = navHostController)
            }
        )

    }, content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isLoading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    list.isNotEmpty() -> {
                        val filteredList = list.filter { it.restaurantId == order?.restaurantId }

                        if (filteredList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth().fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.not_items),
                                        contentDescription = "No items",
                                        modifier = Modifier.size(250.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else {

                            items(filteredList) { product ->
                                product._rate?.let {
                                    product.p_name?.let { it1 ->
                                        product._description?.let { it2 ->
                                            product.image_url?.let { it3 ->
                                                CommonCard(it,
                                                    it1, it2, it3, {
                                                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "product",
                                                            product
                                                        )
                                                        navHostController.navigate("Details")
                                                    }) {
                                                    selectedItem = product
                                                    showBottomSheet = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                    else -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.not_items),
                                    contentDescription = "No items",
                                    modifier = Modifier.size(250.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }


            }

            if (showBottomSheet) {
                selectedItem?.let {
                    if (activity != null) {
                        SimpleBottomSheet(
                            context = context,
                            navHostController,
                            db = db,
                            snackbarHostState = snackbarHostState,
                            coroutineScope = coroutineScope,
                            onDismiss = { showBottomSheet = false },
                            name = SharedPrefManager.getString("UserName"),
                            item = it, 0.0, activity, 0.0, "",
                            onOrderPlaced = { addedCount ->
                                cartCount += addedCount
                            }
                        )
                    }
                }
            }
        }
    })
}


fun getProduct(db: FirebaseFirestore, onResult: (List<Product>) -> Unit) {
    db.collection("product")
        .addSnapshotListener { result, _ ->
            val productList = mutableListOf<Product>()
            if (result != null) {
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
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
                                        CommonUtil.showSnackbar(
                                            "Quantity must be at least one",
                                            false,
                                            snackbarHostState,
                                            coroutineScope
                                        )

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

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val currentTime: String = sdf.format(Date())
                            val currentDate: String = date.format(Date())

                            val order = SendOrder(
                                productId = item.id ?: "",
                                productName = item.p_name.toString(),
                                productDetails = item._description.toString(),
                                quantity = count,
                                orderId = "",
                                currentTime = currentTime,
                                customerName = name,
                                deliveryBoye = "",
                                customerId = SharedPrefManager.getString("UserId"),
                                amount = "${(item._rate?.toDouble())?.times(count)}",
                                status = "Pending",
                                orderLongitude = longitude,
                                orderLatitude = latitude,
                                orderDate = currentDate,
                                orderAddress = orderAddress,
                                productImage = item.image_url,
                            )
                            isLoading = true
                            db.collection("CartItems")
                                .add(order)
                                .addOnSuccessListener { _ ->
                                    coroutineScope.launch {
                                        isLoading = false
                                        CommonUtil.showSnackbar(
                                            "Order is Added in the cart",
                                            true,
                                            snackbarHostState,
                                            coroutineScope
                                        )
                                        order.quantity?.let { onOrderPlaced(it) }
                                        onDismiss()

                                    }
                                }
                                .addOnFailureListener { e ->
                                    coroutineScope.launch {
                                        isLoading = false
                                        CommonUtil.showSnackbar(
                                            "Something Went Wrong $e",
                                            false,
                                            snackbarHostState,
                                            coroutineScope
                                        )
                                        onDismiss()
                                    }

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
                        CommonUtils().CommonText(
                            "Please wait...",
                            fontWeight = FontWeight.W600,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CommonUtils().CommonText(
                                "Add to cart ",
                                fontWeight = FontWeight.W400,
                                color = Color.White,
                                fontSize = 14
                            )
                            CommonUtils().CommonText(
                                " ₹ ${(item._rate?.toInt() ?: 0) * count}",
                                fontWeight = FontWeight.W400,
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
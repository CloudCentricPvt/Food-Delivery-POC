package com.cccinfotech.fooddeliverypoc.screens.splash

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.broadcast.NetworkReceiver
import com.cccinfotech.fooddeliverypoc.worker.MyWorker
import com.cccinfotech.fooddeliverypoc.navgraph.AppNavGraph
import com.cccinfotech.fooddeliverypoc.ui.theme.FoodDeliveryPOCTheme
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity(), PaymentResultListener {

    private lateinit var networkReceiver: NetworkReceiver

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodDeliveryPOCTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier) {
                    if (!CommonUtils().isNetworkAvailable(this)) {
                        Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
                    }
                    val context = LocalContext.current
                    AppNavGraph(navController = navController)
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    SharedPrefManager.init(context)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        //network receiver notification
        networkReceiver = NetworkReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        //for schedule food message
        val workRequest =
            PeriodicWorkRequestBuilder<MyWorker>(15, TimeUnit.MINUTES) // Minimum is 15 min
                .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MyTask",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
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

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(true) {
        delay(3000L)
        if (SharedPrefManager.getBoolean("IsLogin")) {
            navController.navigate("Menu") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("Auth") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .customGradientBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .wrapContentSize(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.food),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun Modifier.customGradientBackground(): Modifier = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFFFFF)
        )
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    FoodDeliveryPOCTheme {
        val navController = rememberNavController()
        SplashScreen(navController)
    }
}
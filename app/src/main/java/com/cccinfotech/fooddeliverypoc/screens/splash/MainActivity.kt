package com.cccinfotech.fooddeliverypoc.screens.splash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.navgraph.AppNavGraph
import com.cccinfotech.fooddeliverypoc.ui.theme.FoodDeliveryPOCTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodDeliveryPOCTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier) {
                    AppNavGraph(navController = navController)
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(true) {
        delay(3000L)
        navController.navigate("onBoarding") {
            popUpTo("splash") { inclusive = true }
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
            Spacer(Modifier.height(5.dp))
            Spacer(Modifier.height(5.dp))
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
        val navController= rememberNavController()
        SplashScreen(navController)
    }
}
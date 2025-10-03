package com.cccinfotech.fooddeliverypoc.navgraph

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cccinfotech.fooddeliverypoc.screens.auth.AuthScreen
import com.cccinfotech.fooddeliverypoc.screens.auth.SignupScreen
import com.cccinfotech.fooddeliverypoc.screens.home.HomeScreen
import com.cccinfotech.fooddeliverypoc.screens.home.OrderCartListScreen
import com.cccinfotech.fooddeliverypoc.screens.home.OrderListScreen
import com.cccinfotech.fooddeliverypoc.screens.home.PaymentScreen
import com.cccinfotech.fooddeliverypoc.screens.map.MapScreen
import com.cccinfotech.fooddeliverypoc.screens.map.ViewMapScreen
import com.cccinfotech.fooddeliverypoc.screens.searchplace.SearchPlaces
import com.cccinfotech.fooddeliverypoc.screens.splash.SplashScreen
import com.cccinfotech.fooddeliverypoc.testfile.TestMap


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("Auth") {
            AuthScreen(navController)
        }
        composable("Menu") {
            HomeScreen(navController)
        }
        composable("MapScreen") {
            MapScreen(navController)
        }
        composable("ViewMapScreen") {
            ViewMapScreen(navController)
        }
        composable("SignUp") {
            SignupScreen(navController)
        }
        composable("Order"){
            OrderListScreen(navController)
        }
        composable("Cart"){
            OrderCartListScreen(navController)
        }
        composable("Test"){
            TestMap()
        }
        composable("Place"){
            SearchPlaces()
        }
        composable("Payment"){
            PaymentScreen(navController)
        }


    }
}
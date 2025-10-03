package com.cccinfotech.fooddeliverypoc.nav_screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cccinfotech.fooddeliverypoc.screens.auth.AuthScreen

@Composable
fun AppNavigation(navController: NavHostController) { // make non-null
    NavHost(navController = navController, startDestination = "login_screen") {

        composable("login_screen") {

            AuthScreen(navController)

        }
    }
}
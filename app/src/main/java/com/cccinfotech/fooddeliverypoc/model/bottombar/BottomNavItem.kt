package com.cccinfotech.fooddeliverypoc.model.bottombar

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

package com.cccinfotech.fooddeliverypoc.model.restaurent

import java.io.Serializable

data class Restaurant(
    val restaurantId: String? = "",
    val name: String? = "",
    val rating: String? = "",
    val latitude: String? = "",
    val longitude: String? = "",
    val restaurantAddress: String? = "",
    val imageUrl: String? = "",
    val category: String? = "",

    ) : Serializable

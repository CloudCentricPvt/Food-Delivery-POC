package com.cccinfotech.fooddeliverypoc.model.product

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class Product(
    val p_name: String? = "",
    val _rate: String? = "",
    val _description: String? = "",
    val quantity: String? = "",
    @get:Exclude var id: String? = "",
    val image_url: String? = "",
    val category: String? = "",
    val restaurantId: String? = ""

) : Serializable


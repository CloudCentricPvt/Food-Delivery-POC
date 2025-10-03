package com.cccinfotech.fooddeliverypoc.model.product

import com.google.firebase.firestore.Exclude

data class Product(
    val p_name: String ?=null,
    val _rate: String? =null,
    val _description: String?=null,
    val quantity:String?=null,
    @get:Exclude var id: String? = null,
    val image_url:String?=null

)


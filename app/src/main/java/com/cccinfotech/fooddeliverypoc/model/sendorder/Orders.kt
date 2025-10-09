package com.cccinfotech.fooddeliverypoc.model.sendorder

import java.io.Serializable

data class Orders(
    val orderNumber: String? = null,
    val orderId: String? = null,
    val customerName: String? = null,
    val customerId: String? = null,
    val orderDate: String? = null,
    val currentTime: String? = null,
    val status: String? = null,
    val orderAddress: String? = null,
    val orderLatitude: Double? = null,
    val orderLongitude: Double? = null,
    val dBoy_Id: String? = null,
    val deliveryBoye: String? = null,
    val productImage: String? = null,
    val deliveryTimeOTP: String? = null,

    // Fix: Use List<Item> instead of Map<String, Item>
    val items: List<Item>? = null
):Serializable

data class Item(
    val productName: String = "",
    val quantity: Int = 0,
    val amount: String = "",
    val currentTime: String = "",
    val orderDate: String = "",
    val productImage: String = "",
    val productDetails: String = "",

    ):Serializable

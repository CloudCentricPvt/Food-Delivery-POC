package com.cccinfotech.fooddeliverypoc.model.sendorder

import java.io.Serializable

data class SendOrder(
    val orderId:String?="",
    val productId: String = "",
    val productName: String = "",
    val productDetails: String = "",
    val quantity: Int?=0,
    val currentTime: String? = "",
    val customerName:String?="",
    val deliveryBoye:String?="",
    val customerId:String?="",
    val amount:String?="",
    val status:String?="",
    val orderLatitude:Double?=0.0,
    val orderLongitude:Double?=0.0,
    val orderDate:String?="",
    val orderAddress:String?="",
    val productImage:String?="",
    val orderNumber:String?="",
    var selectedItem:Boolean?=false,
):Serializable

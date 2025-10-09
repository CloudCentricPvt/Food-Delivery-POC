package com.cccinfotech.fooddeliverypoc.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.cccinfotech.fooddeliverypoc.utils.LocationHelper
import com.cccinfotech.fooddeliverypoc.utils.OrderRepository

class DeliveryBoyViewModel : ViewModel() {

    fun sendLocation(orderId: String, context: Context) {
        val locationHelper = LocationHelper(context)
        locationHelper.getCurrentLocation { lat, lng ->
            OrderRepository.updateDeliveryLocation(orderId, lat, lng)
        }
    }

}



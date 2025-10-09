package com.cccinfotech.fooddeliverypoc.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cccinfotech.fooddeliverypoc.viewmodel.MapViewModel

class MapViewModelFactory(private val orderId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(orderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

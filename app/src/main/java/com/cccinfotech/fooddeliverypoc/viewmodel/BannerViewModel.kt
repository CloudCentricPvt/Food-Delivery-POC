package com.cccinfotech.fooddeliverypoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cccinfotech.fooddeliverypoc.model.banner.Banner
import com.cccinfotech.fooddeliverypoc.repository.BannerRepository
import com.cccinfotech.fooddeliverypoc.sealed.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BannerViewModel(
    private val repository: BannerRepository
) : ViewModel() {

    private val _bannerState = MutableStateFlow<Resource<List<Banner>>>(Resource.Loading)
    val bannerState: StateFlow<Resource<List<Banner>>> = _bannerState

    private val _categoryState = MutableStateFlow<Resource<List<String>>>(Resource.Loading)
    val categoryState: StateFlow<Resource<List<String>>> = _categoryState

    fun loadOffers() {
        viewModelScope.launch {
            _bannerState.value = Resource.Loading
            try {
                val banners = repository.fetchOffers()
                Log.d("Ban", "$banners")
                _bannerState.value = Resource.Success(banners)
            } catch (e: Exception) {
                _bannerState.value = Resource.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun getCategory() {
        viewModelScope.launch {
            _categoryState.value = Resource.Loading
            try {
                val banners = repository.fetchCategories()
                _categoryState.value = Resource.Success(banners)
            } catch (e: Exception) {
                _categoryState.value = Resource.Error(e.message ?: "Something went wrong")
            }
        }

    }

}
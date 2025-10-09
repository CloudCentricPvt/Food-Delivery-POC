package com.cccinfotech.fooddeliverypoc.repository

import com.cccinfotech.fooddeliverypoc.firebaseservices.FirebaseService
import com.cccinfotech.fooddeliverypoc.model.banner.Banner

class BannerRepository(private val firebaseService: FirebaseService) {

    suspend fun fetchOffers(): List<Banner> {
        val snapshot = firebaseService.getDataRaw("banner")
        return snapshot.mapNotNull { doc ->
            doc.toObject(Banner::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchCategories(): List<String> {
        val document = firebaseService.getDocument("products/erhNs2hooW4CQu9IPioi")
        val categoryList = document.get("category") as? List<String>
        return categoryList ?: emptyList()
    }


}

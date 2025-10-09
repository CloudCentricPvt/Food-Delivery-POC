package com.cccinfotech.fooddeliverypoc.firebaseservices

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseService {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getDataRaw(collectionName: String) =
        db.collection(collectionName).get().await().documents

    suspend fun getDocument(path: String) =
        db.document(path).get().await()



    suspend fun <T> getData(collectionName: String, clazz: Class<T>): List<T> {
        val snapshot = db.collection(collectionName).get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(clazz)
        }
    }
}

// CategoryViewModelFactory.kt
package com.example.cashroyale.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.Services.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Factory class for creating instances of [CategoryListViewModel].
 * Provides the necessary dependencies to the ViewModel during its creation.
 */
class CategoryViewModelFactory(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore, // Pass FirebaseFirestore instance
    private val fireStoreService: FireStore, // Pass the FireStore service instance
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

//    private val categoryDao: CategoryDAO by lazy {
//        AppDatabase.getDatabase(context.applicationContext).categoryDAO()
//    }


    /**
     * Creates a new instance of the specified ViewModel class.
     * This factory is specifically designed to create [CategoryListViewModel].
     * @return A new instance of [CategoryListViewModel] if the modelClass matches,
     * otherwise, it throws an IllegalArgumentException.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryListViewModel(application, auth, db, fireStoreService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
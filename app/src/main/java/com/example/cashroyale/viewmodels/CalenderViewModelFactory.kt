package com.example.cashroyale.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashroyale.Services.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CalenderViewModelFactory(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore, // Pass FirebaseFirestore instance
    private val fireStoreService: FireStore, // Pass the FireStore service instance
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalenderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Ensure parameters match CalenderViewModel's constructor
            return CalenderViewModel(application, auth, db, fireStoreService) as T
        }
        return super.create(modelClass)
    }
}
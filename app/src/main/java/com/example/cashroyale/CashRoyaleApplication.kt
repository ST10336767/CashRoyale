package com.example.cashroyale

import android.app.Application
import com.google.firebase.FirebaseApp

class CashRoyaleApplication : Application(){
    override fun onCreate(){
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
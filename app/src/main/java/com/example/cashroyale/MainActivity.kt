package com.example.cashroyale

// Kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * The main activity of the CashRoyale application.
 * Sets up the bottom navigation and links it to the navigation host fragment.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get a reference to the BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Get a reference to the NavHostFragment from the layout
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        // If the NavHostFragment is found, set up the navigation controller
        navHostFragment?.let {
            val navController = it.navController
            // Connect the BottomNavigationView to the NavController
            NavigationUI.setupWithNavController(bottomNavigationView, navController)
        }
    }
}